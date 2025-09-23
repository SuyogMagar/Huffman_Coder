import React, { useEffect, useRef } from 'react';

const Starfield = () => {
    const canvasRef = useRef(null);
    const animationRef = useRef(null);
    const starsRef = useRef([]);
    const meteorsRef = useRef([]);
    const dpiRef = useRef(window.devicePixelRatio || 1);

    useEffect(() => {
        const canvas = canvasRef.current;
        if (!canvas) return;
        const ctx = canvas.getContext('2d');

        function resize() {
            const { innerWidth, innerHeight } = window;
            const dpi = dpiRef.current;
            canvas.width = Math.floor(innerWidth * dpi);
            canvas.height = Math.floor(innerHeight * dpi);
            canvas.style.width = innerWidth + 'px';
            canvas.style.height = innerHeight + 'px';
        }

        function spawnStars() {
            const { width, height } = canvas;
            const count = Math.min(300, Math.floor((width * height) / (1400 * 900) * 250));
            starsRef.current = Array.from({ length: count }).map(() => ({
                x: Math.random() * width,
                y: Math.random() * height,
                r: Math.random() * 1.8 + 0.4,
                a: Math.random() * 0.6 + 0.4,
                speed: Math.random() * 0.15 + 0.05,
            }));
        }

        function spawnMeteor() {
            const { width } = canvas;
            const startX = Math.random() * width * 0.8 + width * 0.1;
            const length = Math.random() * 250 + 150;
            meteorsRef.current.push({
                x: startX,
                y: -50 * dpiRef.current,
                vx: -3.2 * dpiRef.current,
                vy: 6.0 * dpiRef.current,
                life: 0,
                maxLife: 120 + Math.random() * 60,
                length,
            });
        }

        function drawStar(s) {
            ctx.globalAlpha = s.a;
            ctx.fillStyle = '#ffffff';
            ctx.beginPath();
            ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2);
            ctx.fill();
        }

        function drawMeteor(m) {
            const grad = ctx.createLinearGradient(m.x, m.y, m.x - m.length, m.y - m.length);
            grad.addColorStop(0, 'rgba(255,255,255,0.9)');
            grad.addColorStop(1, 'rgba(0,173,181,0)');
            ctx.strokeStyle = grad;
            ctx.lineWidth = 2 * dpiRef.current;
            ctx.beginPath();
            ctx.moveTo(m.x, m.y);
            ctx.lineTo(m.x - m.length, m.y - m.length);
            ctx.stroke();
        }

        function update() {
            const { width, height } = canvas;
            ctx.clearRect(0, 0, width, height);

            // Stars twinkle and drift slowly
            starsRef.current.forEach((s) => {
                s.y += s.speed; // subtle vertical drift
                s.a += (Math.random() - 0.5) * 0.02; // twinkle
                if (s.a < 0.2) s.a = 0.2;
                if (s.a > 1) s.a = 1;
                if (s.y > height) s.y = 0;
                drawStar(s);
            });

            // Occasionally spawn a meteor
            if (Math.random() < 0.01 && meteorsRef.current.length < 2) {
                spawnMeteor();
            }

            // Update meteors
            meteorsRef.current = meteorsRef.current.filter((m) => {
                m.x += m.vx;
                m.y += m.vy;
                m.life += 1;
                drawMeteor(m);
                return m.life < m.maxLife && m.y < height + 100 * dpiRef.current;
            });

            animationRef.current = requestAnimationFrame(update);
        }

        resize();
        spawnStars();
        update();
        window.addEventListener('resize', () => { resize(); spawnStars(); });

        return () => {
            cancelAnimationFrame(animationRef.current);
            window.removeEventListener('resize', () => { resize(); spawnStars(); });
        };
    }, []);

    return (
        <canvas
            ref={canvasRef}
            style={{
                position: 'fixed',
                inset: 0,
                zIndex: 0,
                pointerEvents: 'none'
            }}
        />
    );
};

export default Starfield;


