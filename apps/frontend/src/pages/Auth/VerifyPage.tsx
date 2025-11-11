import type { FormEvent } from "react";
import { useState } from "react";
import { apiClient } from "../../api/client";

type VerifyResponse = {
    status: string;
};

export function VerifyPage() {
    const [token, setToken] = useState("");
    const [message, setMessage] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    async function handleSubmit(e: FormEvent) {
        e.preventDefault();
        setMessage(null);
        setError(null);

        try {
            const res = await apiClient.post<VerifyResponse>("/auth/verify", {
                token,
            });
            setMessage(`Verification status: ${res.status}`);
        } catch (err: any) {
            setError(err.message ?? "Verification failed");
        }
    }

    return (
        <div>
            <h2>Verify Email</h2>
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

            {message && <p style={{ color: "green" }}>{message}</p>}
            {error && <p style={{ color: "red" }}>{error}</p>}
        </div>
    );
}
