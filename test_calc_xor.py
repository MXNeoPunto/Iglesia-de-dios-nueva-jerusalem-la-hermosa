import unittest
from calc_xor import xor_string

class TestCalcXor(unittest.TestCase):

    def test_xor_string_basic(self):
        input_str = "hello"
        key = "abc"
        # h ^ a = 104 ^ 97 = 9
        # e ^ b = 101 ^ 98 = 7
        # l ^ c = 108 ^ 99 = 15
        # l ^ a = 108 ^ 97 = 13
        # o ^ b = 111 ^ 98 = 13
        expected = bytearray([9, 7, 15, 13, 13])
        self.assertEqual(xor_string(input_str, key), expected)

    def test_xor_string_empty_input(self):
        input_str = ""
        key = "secret"
        self.assertEqual(xor_string(input_str, key), bytearray())

    def test_xor_string_empty_key(self):
        input_str = "data"
        key = ""
        with self.assertRaises(ValueError) as cm:
            xor_string(input_str, key)
        self.assertEqual(str(cm.exception), "Key cannot be empty")

    def test_xor_string_different_lengths(self):
        # Input shorter than key
        self.assertEqual(xor_string("a", "abc"), bytearray([0])) # a ^ a = 0
        # Input longer than key (already tested in basic, but good to have)
        self.assertEqual(xor_string("abcde", "ab"), bytearray([0, 0, 2, 6, 4]))
        # a ^ a = 0, b ^ b = 0, c ^ a = 99 ^ 97 = 2, d ^ b = 100 ^ 98 = 6, e ^ a = 101 ^ 97 = 4

    def test_xor_string_utf8(self):
        input_str = "Héllö"
        key = "key"
        # H: 72, é: 195 169, l: 108, l: 108, ö: 195 182 (UTF-8)
        res = xor_string(input_str, key)
        # Verify it can be reversed
        reversed_bytes = bytearray()
        key_bytes = key.encode('utf-8')
        for i, b in enumerate(res):
            reversed_bytes.append(b ^ key_bytes[i % len(key_bytes)])
        self.assertEqual(reversed_bytes.decode('utf-8'), input_str)

    def test_xor_string_symmetry(self):
        input_str = "This is a secret message"
        key = "SuperSecretKey"
        encrypted = xor_string(input_str, key)
        # To decrypt, we apply XOR again.
        # But xor_string takes a string as input, not bytes.
        # However, we can manually verify the bytes.
        decrypted_bytes = bytearray()
        key_bytes = key.encode('utf-8')
        for i, b in enumerate(encrypted):
            decrypted_bytes.append(b ^ key_bytes[i % len(key_bytes)])

        self.assertEqual(decrypted_bytes.decode('utf-8'), input_str)

if __name__ == '__main__':
    unittest.main()
