import type { FormEvent } from "react";
import { useState, useEffect, useRef } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import { apiClient } from "../../api/client";

export function VerifyPage() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const tokenFromUrl = searchParams.get("token");

    const [token, setToken] = useState(tokenFromUrl || "");
    const [message, setMessage] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [isVerifying, setIsVerifying] = useState(false);
    const hasAttemptedVerification = useRef(false);

    // Auto-verify if token is in URL (only once)
    useEffect(() => {
        if (tokenFromUrl && !hasAttemptedVerification.current) {
            hasAttemptedVerification.current = true;
            verifyToken(tokenFromUrl);
        }
    }, [tokenFromUrl]);

    async function verifyToken(tokenValue: string) {
        setIsVerifying(true);
        setMessage(null);
        setError(null);

        try {
            await apiClient.post<void>("/auth/verify", {
                token: tokenValue,
            });
            setMessage("Email verified successfully! You can now log in.");
            setTimeout(() => {
                navigate("/login");
            }, 2000);
        } catch (err: any) {
            setError(err.message ?? "Verification failed");
        } finally {
            setIsVerifying(false);
        }
    }

    async function handleSubmit(e: FormEvent) {
        e.preventDefault();
        verifyToken(token);
    }

    return (
        <div>
            <h2>Verify Email</h2>

            {isVerifying && <p>Verifying your email...</p>}
            {message && <p style={{ color: "green" }}>{message}</p>}
            {error && <p style={{ color: "red" }}>{error}</p>}

            {!tokenFromUrl && !isVerifying && (
                <form onSubmit={handleSubmit} style={{ maxWidth: 400, display: "flex", flexDirection: "column", gap: "0.5rem" }}>
                    <label>
                        Verification Token
                        <input
                            type="text"
                            value={token}
                            onChange={(e) => setToken(e.target.value)}
                            required
                        />
                    </label>

                    <button type="submit">Verify</button>
                </form>
            )}
        </div>
    );
}
