import React from 'react';
import Navbar from '../components/Navbar';
import Starfield from '../components/Starfield';

const About = () => {
    return (
        <>
            <Starfield />
            <Navbar />
            <section className="about simple">
                <div className="about-hero">
                    <h1>About Huffman Coder</h1>
                    <p className="about-sub">A minimal tool to compress and decompress files with a modern, hybrid engine.</p>
                </div>

                <div className="about-content">
                    <p>
                        Huffman Coder started as a learning project to visualize classic Huffman compression. It now
                        offers a Smart Compress option that automatically picks efficient algorithms for your files
                        while keeping the original Huffman mode available for study and demos.
                    </p>
                    <p>
                        PDFs are optimized safely, then tested with modern codecs (ZSTD, Brotli, LZ4, Deflate) and the
                        smallest result is returned with helpful stats. The UI stays intentionally simple—upload, press
                        compress, and download.
                    </p>
                </div>

                <div className="about-footer">
                    <a className="btn" href="/">Back to App</a>
                </div>
            </section>
        </>
    );
};

export default About;


