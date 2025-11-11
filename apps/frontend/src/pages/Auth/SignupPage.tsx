import type { FormEvent } from "react";
import { useState } from "react";
import { apiClient } from "../../api/client";

type SignupResponse = {
    status: string;
    jwt: string | null;
};

export function SignupPage() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [campusDomain, setCampusDomain] = useState("csun.edu");
    const [message, setMessage] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    async function handleSubmit(e: FormEvent) {
        e.preventDefault();
        setMessage(null);
        setError(null);

        try {
            const res = await apiClient.post<SignupResponse>("/auth/signup", {
                email,
                password,
                campusDomain,
            });
            setMessage(
                `Signup status: ${res.status}. Check backend logs for verification token.`
            );
        } catch (err: any) {
            setError(err.message ?? "Signup failed");
        }
    }

    return (
        <div>
            <h2>Sign Up</h2>
            <form onSubmit={handleSubmit} style={{ maxWidth: 400, display: "flex", flexDirection: "column", gap: "0.5rem" }}>
                <label>
                    Email (.edu)
                    <input
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </label>

                <label>
                    Password
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </label>

                <label>
                    Campus Domain
                    <input
                        type="text"
                        value={campusDomain}
                        onChange={(e) => setCampusDomain(e.target.value)}
                        required
                    />
                </label>

                <button type="submit">Create account</button>
            </form>

            {message && <p style={{ color: "green" }}>{message}</p>}
            {error && <p style={{ color: "red" }}>{error}</p>}
        </div>
    );
}
