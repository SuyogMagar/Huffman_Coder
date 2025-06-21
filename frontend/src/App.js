import React, { useState } from 'react';
import axios from 'axios';
import './App.css';

function App() {
    const [compressFile, setCompressFile] = useState(null);
    const [decompressFile, setDecompressFile] = useState(null);

    const handleCompressFileChange = (e) => {
        setCompressFile(e.target.files[0]);
    };

    const handleDecompressFileChange = (e) => {
        setDecompressFile(e.target.files[0]);
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

    return (
        <div className="App">
            <header className="App-header">
                <h1>Huffman Coder</h1>
            </header>

            <main className="coder-section">
                <div className="coder-container">
                    <h2>Compress File</h2>
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
                            <p className="file-name">Selected: {compressFile.name}</p>
                        )}
                    </div>
                    <button
                        className="btn"
                        onClick={handleCompress}
                        disabled={!compressFile}
                    >
                        Compress
                    </button>
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
                            <p className="file-name">Selected: {decompressFile.name}</p>
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
    );
}

export default App;
