#!/usr/bin/env python3
"""Validate Nyxtra's generated sing-box config against the real binary.

The existing unit tests only assert on JSON shape, so a field that upstream
removed (inbound.sniff) passed CI while making every non-game-mode connection
fail. This script reproduces the generator output for the full settings matrix
and feeds it to `sing-box check`.

Usage:
    python3 tools/validate_singbox_config.py /path/to/sing-box
"""
import itertools
import json
import subprocess
import sys
import tempfile
import os

REALITY_PBK = "jNXHt1yRo0vDuchQlIP6Z0ZvjT3KtzVI-T4E7RoLJS0"


def generate(mtu=1280, stack="mixed", zero_routing_sniffing=True, tcp_nodelay=True,
             keepalive=15, per_app=None, whitelist=True, proto="vless",
             transport="ws", is_tls=True, insecure=False,
             reality_pbk="", reality_sid="", reality_fp="chrome"):
    """Mirror of SingBoxConfigGenerator.generate(). Keep in sync with the Kotlin source."""
    cfg = {
        "log": {"level": "info", "timestamp": True},
        "dns": {
            "servers": [
                {"type": "https", "tag": "remote-dns", "server": "1.1.1.1", "detour": "proxy"},
                {"type": "udp", "tag": "direct-dns", "server": "1.1.1.1"},
            ],
            "final": "remote-dns",
            "strategy": "prefer_ipv4",
        },
    }

    tun = {
        "type": "tun", "tag": "tun-in", "interface_name": "nyxtra0",
        "address": ["172.19.0.1/30", "fdfe:dcba:9876::1/126"],
        "mtu": mtu, "auto_route": True, "strict_route": False, "stack": stack,
    }
    if per_app:
        tun["include_package" if whitelist else "exclude_package"] = per_app
    cfg["inbounds"] = [tun]

    out = {"type": proto, "tag": "proxy", "server": "203.0.113.10",
           "server_port": 443, "domain_resolver": "direct-dns"}
    if proto == "vless":
        out["uuid"] = "11111111-2222-3333-4444-555555555555"
        out["packet_encoding"] = "xudp"
    elif proto == "vmess":
        out["uuid"] = "11111111-2222-3333-4444-555555555555"
        out["security"] = "auto"
        out["alter_id"] = 0
        out["packet_encoding"] = "xudp"
    else:
        out["password"] = "secret"

    if is_tls:
        tls = {"enabled": True, "server_name": "sni.example.com", "insecure": insecure,
               "utls": {"enabled": True, "fingerprint": reality_fp or "chrome"}}
        if reality_pbk:
            tls["reality"] = {"enabled": True, "public_key": reality_pbk,
                              "short_id": reality_sid}
        out["tls"] = tls

    host = "bug.example.com"
    if transport == "ws":
        out["transport"] = {"type": "ws", "path": "/nyxtra", "headers": {"Host": host}}
    elif transport == "httpupgrade":
        out["transport"] = {"type": "httpupgrade", "path": "/nyxtra", "host": host}
    elif transport == "grpc":
        out["transport"] = {"type": "grpc", "service_name": "nyxtra"}

    if tcp_nodelay:
        out["tcp_fast_open"] = True
        out["tcp_multi_path"] = False
        out["tcp_keep_alive_interval"] = f"{keepalive}s"

    cfg["outbounds"] = [out, {"type": "direct", "tag": "direct"}]

    sniff_rule = {"action": "sniff"}
    if zero_routing_sniffing:
        sniff_rule["sniffer"] = ["dns"]

    cfg["route"] = {
        "auto_detect_interface": True,
        "default_domain_resolver": "direct-dns",
        "rules": [
            {"ip_is_private": True, "outbound": "direct"},
            sniff_rule,
            {"protocol": "dns", "action": "hijack-dns"},
            {"network": "udp", "port": 443, "action": "reject"},
        ],
        "final": "proxy",
    }
    cfg["experimental"] = {"cache_file": {"enabled": True, "path": "nyxtra-cache.db"}}
    return cfg


def check(singbox, cfg, tmpdir):
    path = os.path.join(tmpdir, "config.json")
    with open(path, "w") as fh:
        json.dump(cfg, fh)
    r = subprocess.run([singbox, "check", "-c", path],
                       capture_output=True, text=True)
    return r.returncode == 0 and not r.stderr.strip(), (r.stderr or r.stdout).strip()


def main():
    if len(sys.argv) < 2:
        print("usage: validate_singbox_config.py /path/to/sing-box", file=sys.stderr)
        return 2
    singbox = sys.argv[1]

    total = 0
    failures = []
    with tempfile.TemporaryDirectory() as tmpdir:
        for mtu, stack, zero, proto, trans, tls, perapp in itertools.product(
            (1280, 1300, 1340), ("system", "mixed", "gvisor"), (True, False),
            ("vless", "vmess", "trojan"), ("ws", "httpupgrade", "grpc", "tcp"),
            (True, False), (None, ["com.example.game"]),
        ):
            if proto == "trojan" and trans in ("httpupgrade", "grpc"):
                continue
            cfg = generate(mtu=mtu, stack=stack, zero_routing_sniffing=zero,
                           proto=proto, transport=trans, is_tls=tls, per_app=perapp)
            total += 1
            ok, err = check(singbox, cfg, tmpdir)
            if not ok:
                failures.append((f"{proto}/{trans} mtu={mtu} stack={stack} "
                                 f"zeroRouting={zero} tls={tls}", err))

        # REALITY must be emitted correctly, it used to be parsed and dropped.
        for trans in ("tcp", "ws", "grpc", "httpupgrade"):
            cfg = generate(proto="vless", transport=trans,
                           reality_pbk=REALITY_PBK, reality_sid="0123abcd")
            total += 1
            ok, err = check(singbox, cfg, tmpdir)
            if not ok:
                failures.append((f"reality/{trans}", err))

    print(f"Validated {total} generated configs")
    if failures:
        print(f"FAILED: {len(failures)}")
        for name, err in failures[:20]:
            print(f"  - {name}")
            print(f"    {err.splitlines()[0] if err else ''}")
        return 1
    print("All configs accepted by sing-box")
    return 0


if __name__ == "__main__":
    sys.exit(main())
