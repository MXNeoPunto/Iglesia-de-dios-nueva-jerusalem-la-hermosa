import base64
import argparse
import os
import sys

def xor_string(input_str, key):
    # This might produce characters not valid in utf-8 if we just use chr(),
    # but since we are doing base64 afterwards, we need bytes.
    # Let's do XOR on bytes to be safe.
    input_bytes = input_str.encode('utf-8')
    key_bytes = key.encode('utf-8')

    xor_bytes = bytearray()
    for i, b in enumerate(input_bytes):
        xor_bytes.append(b ^ key_bytes[i % len(key_bytes)])

    return xor_bytes

def main():
    parser = argparse.ArgumentParser(description="XOR encrypt a string.")
    parser.add_argument("--url", help="The URL to encrypt", default="https://app0102.sonicpanelradio.com/8070/stream")
    parser.add_argument("--key", help="The encryption key")
    args = parser.parse_args()

    key = args.key or os.environ.get("XOR_KEY")

    if not key:
        print("Error: Encryption key must be provided via --key or XOR_KEY environment variable.", file=sys.stderr)
        sys.exit(1)

    url = args.url

    xor_res = xor_string(url, key)
    encoded_res = base64.b64encode(xor_res).decode('utf-8')

    print(f"Encrypted: {encoded_res}")

if __name__ == "__main__":
    main()
