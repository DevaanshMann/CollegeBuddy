import type { FormEvent } from "react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiClient } from "../../api/client";
import { JWT_STORAGE_KEY } from "../../config";

type LoginResponse = {
    status: string;
    jwt: string | null;
};

export function LoginPage() {
    const navigate = useNavigate();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);

    async function handleSubmit(e: FormEvent) {
        e.preventDefault();
        setError(null);

        try {
            const res = await apiClient.post<LoginResponse>("/auth/login", {
                email,
                password,
            });

            if (!res.jwt) {
                setError("No JWT returned. Is the account verified?");
                return;
            }

            localStorage.setItem(JWT_STORAGE_KEY, res.jwt);
            navigate("/profile");
        } catch (err: any) {
            setError(err.message ?? "Login failed");
        }
    }

    return (
        <div>
            <h2>Login</h2>
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

                <button type="submit">Log in</button>
            </form>

            {error && <p style={{ color: "red" }}>{error}</p>}
        </div>
    );
}
