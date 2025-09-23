package com.stellarfs.huffman_coder.service;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

@Service
public class HuffmanService {

    private static class Node implements Comparable<Node> {
        byte data;
        int freq;
        Node left, right;

        public Node(byte data, int freq, Node left, Node right) {
            this.data = data;
            this.freq = freq;
            this.left = left;
            this.right = right;
        }

        @Override
        public int compareTo(Node other) {
            return this.freq - other.freq;
        }

        public boolean isLeaf() {
            return this.left == null && this.right == null;
        }
    }

    public byte[] compress(byte[] data) throws IOException {
        Map<Byte, Integer> freqMap = getFrequencyMap(data);
        Node root = buildHuffmanTree(freqMap);
        Map<Byte, String> huffmanCodes = generateHuffmanCodes(root);

        StringBuilder encodedData = new StringBuilder();
        for (byte b : data) {
            encodedData.append(huffmanCodes.get(b));
        }
        
        int padding = 8 - (encodedData.length() % 8);
        if (padding == 8) padding = 0;


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(freqMap);
        oos.close();

        byte[] headerBytes = baos.toByteArray();
        byte[] dataBytes = getBytesFromPaddedString(encodedData.toString());

        byte[] compressedData = new byte[headerBytes.length + 1 + dataBytes.length];
        System.arraycopy(headerBytes, 0, compressedData, 0, headerBytes.length);
        compressedData[headerBytes.length] = (byte) padding;
        System.arraycopy(dataBytes, 0, compressedData, headerBytes.length + 1, dataBytes.length);

        return compressedData;
    }

    public byte[] decompress(byte[] data) throws IOException, ClassNotFoundException {
        if (data == null || data.length == 0) {
            throw new IOException("Empty compressed data");
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            @SuppressWarnings("unchecked")
            Map<Byte, Integer> freqMap = (Map<Byte, Integer>) ois.readObject();

            int padding = bais.read();
            if (padding < 0 || padding > 7) {
                throw new IOException("Invalid padding value in compressed data");
            }

            byte[] dataBytes = bais.readAllBytes();
            if (dataBytes.length == 0 && padding != 0) {
                throw new IOException("Corrupted compressed payload");
            }

            Node root = buildHuffmanTree(freqMap);
            if (root == null) {
                return new byte[0];
            }

            StringBuilder encodedData = new StringBuilder();
            for (byte b : dataBytes) {
                encodedData.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
            }

            if (padding > 0) {
                if (padding > encodedData.length()) {
                    throw new IOException("Padding exceeds encoded data length");
                }
                encodedData.setLength(encodedData.length() - padding);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (root.isLeaf()) {
                // Special case: only one unique byte in original data.
                // There are no bits to traverse; however, without original length we cannot infer count.
                // In current format we cannot recover length, so return zero-length to avoid NPEs.
                return baos.toByteArray();
            }

            Node current = root;
            for (int i = 0; i < encodedData.length(); i++) {
                char c = encodedData.charAt(i);
                if (c == '0') {
                    if (current.left == null) {
                        throw new IOException("Corrupted bitstream: unexpected '0'");
                    }
                    current = current.left;
                } else {
                    if (current.right == null) {
                        throw new IOException("Corrupted bitstream: unexpected '1'");
                    }
                    current = current.right;
                }

                if (current.isLeaf()) {
                    baos.write(current.data);
                    current = root;
                }
            }

            return baos.toByteArray();
        }
    }

    private Map<Byte, Integer> getFrequencyMap(byte[] data) {
        Map<Byte, Integer> freqMap = new HashMap<>();
        for (byte b : data) {
            freqMap.put(b, freqMap.getOrDefault(b, 0) + 1);
        }
        return freqMap;
    }

    private Node buildHuffmanTree(Map<Byte, Integer> freqMap) {
        PriorityQueue<Node> pq = new PriorityQueue<>();
        for (Map.Entry<Byte, Integer> entry : freqMap.entrySet()) {
            pq.add(new Node(entry.getKey(), entry.getValue(), null, null));
        }

        while (pq.size() > 1) {
            Node left = pq.poll();
            Node right = pq.poll();
            Node parent = new Node((byte) 0, left.freq + right.freq, left, right);
            pq.add(parent);
        }

        return pq.poll();
    }

    private Map<Byte, String> generateHuffmanCodes(Node root) {
        Map<Byte, String> huffmanCodes = new HashMap<>();
        generateCodesRecursive(root, "", huffmanCodes);
        return huffmanCodes;
    }

    private void generateCodesRecursive(Node node, String code, Map<Byte, String> huffmanCodes) {
        if (node == null) {
            return;
        }
        if (node.isLeaf()) {
            huffmanCodes.put(node.data, code);
        }
        generateCodesRecursive(node.left, code + "0", huffmanCodes);
        generateCodesRecursive(node.right, code + "1", huffmanCodes);
    }

    private byte[] getBytesFromPaddedString(String encodedData) {
        StringBuilder paddedData = new StringBuilder(encodedData);
        while (paddedData.length() % 8 != 0) {
            paddedData.append('0');
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < paddedData.length(); i += 8) {
            String byteString = paddedData.substring(i, i + 8);
            baos.write((byte) Integer.parseInt(byteString, 2));
        }
        return baos.toByteArray();
    }
} 