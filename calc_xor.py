import base64

def xor_string(input_str, key):
    if not key:
        raise ValueError("Key cannot be empty")

    # This might produce characters not valid in utf-8 if we just use chr(),
    # but since we are doing base64 afterwards, we need bytes.
    # Let's do XOR on bytes to be safe.
    input_bytes = input_str.encode('utf-8')
    key_bytes = key.encode('utf-8')

    xor_bytes = bytearray()
    for i, b in enumerate(input_bytes):
        xor_bytes.append(b ^ key_bytes[i % len(key_bytes)])

    return xor_bytes

if __name__ == "__main__":
    url = "https://app0102.sonicpanelradio.com/8070/stream"
    key = "NuevaJerusalemKey"

    xor_res = xor_string(url, key)
    encoded_res = base64.b64encode(xor_res).decode('utf-8')

    print(f"Key: {key}")
    print(f"Encrypted: {encoded_res}")
