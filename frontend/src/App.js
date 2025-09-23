import React, { useState } from 'react';
import axios from 'axios';
import Navbar from './components/Navbar';
import Starfield from './components/Starfield';
import './App.css';

function App() {
    const [compressFile, setCompressFile] = useState(null);
    const [decompressFile, setDecompressFile] = useState(null);
    const [smartCompressFile, setSmartCompressFile] = useState(null);
    const [huffmanStats, setHuffmanStats] = useState(null);
    const [smartStats, setSmartStats] = useState(null);

    const handleCompressFileChange = (e) => {
        setCompressFile(e.target.files[0]);
        setHuffmanStats(null);
    };

    const handleDecompressFileChange = (e) => {
        setDecompressFile(e.target.files[0]);
    };

    const handleSmartCompressFileChange = (e) => {
        setSmartCompressFile(e.target.files[0]);
        setSmartStats(null);
    };

    const handleCompress = async () => {
        if (!compressFile) {
            alert("Please select a file to compress.");
            return;
        }

        const formData = new FormData();
        formData.append('file', compressFile);

        try {
            const response = await axios.post('/api/compress', formData, {
                responseType: 'blob',
            });

            // Calculate compression statistics
            const originalSize = compressFile.size;
            const compressedSize = response.data.size;
            const compressionRatio = ((originalSize - compressedSize) / originalSize * 100).toFixed(2);
            const spaceSaved = originalSize - compressedSize;

            setHuffmanStats({
                method: 'HUFFMAN',
                ratio: compressionRatio + '%',
                originalSize: originalSize,
                compressedSize: compressedSize,
                spaceSaved: spaceSaved
            });

            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', compressFile.name + '.huf');
            document.body.appendChild(link);
            link.click();
            link.remove();

        } catch (error) {
            console.error("Error compressing file:", error);
            alert("Error compressing file. Please check the console for details.");
        }
    };

    const handleDecompress = async () => {
        if (!decompressFile) {
            alert("Please select a file to decompress.");
            return;
        }

        const formData = new FormData();
        formData.append('file', decompressFile);

        try {
            const response = await axios.post('/api/decompress', formData, {
                responseType: 'blob',
            });

            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', decompressFile.name.replace('.huf', ''));
            document.body.appendChild(link);
            link.click();
            link.remove();

        } catch (error) {
            console.error("Error decompressing file:", error);
            alert("Error decompressing file. Please check the console for details.");
        }
    };

    const handleSmartCompress = async () => {
        if (!smartCompressFile) {
            alert("Please select a file to compress.");
            return;
        }

        const formData = new FormData();
        formData.append('file', smartCompressFile);

        try {
            const response = await axios.post('/api/smart-compress', formData, {
                responseType: 'blob',
            });

            // Extract compression statistics from headers
            const compressionMethod = response.headers['x-compression-method'];
            const compressionRatio = response.headers['x-compression-ratio'];
            const originalSize = parseInt(response.headers['x-original-size']) || 0;
            const compressedSize = parseInt(response.headers['x-compressed-size']) || 0;
            const spaceSaved = parseInt(response.headers['x-space-saved']) || 0;

            setSmartStats({
                method: compressionMethod,
                ratio: compressionRatio,
                originalSize: originalSize,
                compressedSize: compressedSize,
                spaceSaved: spaceSaved
            });

            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', smartCompressFile.name + '.compressed');
            document.body.appendChild(link);
            link.click();
            link.remove();

        } catch (error) {
            console.error("Error compressing file:", error);
            alert("Error compressing file. Please check the console for details.");
        }
    };

    return (
        <>
            <Starfield />
            <Navbar />
            <div className="App">

                <main className="coder-section">
                    <div className="coder-container">
                        <h2>Huffman Compress</h2>
                        <div className="file-input-container">
                            <input
                                type="file"
                                id="compress-file"
                                className="file-input"
                                onChange={handleCompressFileChange}
                            />
                            <label htmlFor="compress-file" className="file-label">
                                Choose File
                            </label>
                            {compressFile && (
                                <p className="file-name">Selected: {compressFile.name} ({(compressFile.size / 1024).toFixed(2)} KB)</p>
                            )}
                        </div>
                        <button
                            className="btn"
                            onClick={handleCompress}
                            disabled={!compressFile}
                        >
                            Compress
                        </button>
                        {huffmanStats && (
                            <div className="compression-stats">
                                <h3>Compression Results:</h3>
                                <p><strong>Method:</strong> {huffmanStats.method}</p>
                                <p><strong>Compression Ratio:</strong> {huffmanStats.ratio}</p>
                                <p><strong>Original Size:</strong> {(huffmanStats.originalSize / 1024).toFixed(2)} KB</p>
                                <p><strong>Compressed Size:</strong> {(huffmanStats.compressedSize / 1024).toFixed(2)} KB</p>
                                <p><strong>Space Saved:</strong> {(huffmanStats.spaceSaved / 1024).toFixed(2)} KB</p>
                            </div>
                        )}
                    </div>

                    <div className="coder-container">
                        <h2>Smart Compress</h2>
                        <div className="file-input-container">
                            <input
                                type="file"
                                id="smart-compress-file"
                                className="file-input"
                                accept=".pdf,.txt,.doc,.docx,.jpg,.jpeg,.png,.gif,.mp4,.mp3,.zip,.rar"
                                onChange={handleSmartCompressFileChange}
                            />
                            <label htmlFor="smart-compress-file" className="file-label">
                                Choose File
                            </label>
                            {smartCompressFile && (
                                <p className="file-name">Selected: {smartCompressFile.name} ({(smartCompressFile.size / 1024).toFixed(2)} KB)</p>
                            )}
                        </div>
                        <button
                            className="btn"
                            onClick={handleSmartCompress}
                            disabled={!smartCompressFile}
                        >
                            Smart Compress
                        </button>
                        {smartStats && (
                            <div className="compression-stats">
                                <h3>Compression Results:</h3>
                                <p><strong>Method:</strong> {smartStats.method}</p>
                                <p><strong>Compression Ratio:</strong> {smartStats.ratio}</p>
                                <p><strong>Original Size:</strong> {(smartStats.originalSize / 1024).toFixed(2)} KB</p>
                                <p><strong>Compressed Size:</strong> {(smartStats.compressedSize / 1024).toFixed(2)} KB</p>
                                <p><strong>Space Saved:</strong> {(smartStats.spaceSaved / 1024).toFixed(2)} KB</p>
                            </div>
                        )}
                    </div>

                    <div className="coder-container">
                        <h2>Decompress File</h2>
                        <div className="file-input-container">
                            <input
                                type="file"
                                id="decompress-file"
                                className="file-input"
                                onChange={handleDecompressFileChange}
                            />
                            <label htmlFor="decompress-file" className="file-label">
                                Choose File
                            </label>
                            {decompressFile && (
                                <p className="file-name">Selected: {decompressFile.name} ({(decompressFile.size / 1024).toFixed(2)} KB)</p>
                            )}
                        </div>
                        <button
                            className="btn"
                            onClick={handleDecompress}
                            disabled={!decompressFile}
                        >
                            Decompress
                        </button>
                    </div>
                </main>
            </div>
        </>
    );
}

export default App;
