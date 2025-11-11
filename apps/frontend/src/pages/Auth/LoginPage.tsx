import type { FormEvent } from "react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiClient } from "../../api/client";
import { JWT_STORAGE_KEY } from "../../config";

type LoginResponse = {
    status: string;
    jwt: string;
};

export function LoginPage() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    async function handleSubmit(e: FormEvent) {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            const res = await apiClient.post<LoginResponse>("/auth/login", {
                email,
                password,
            });

            localStorage.setItem(JWT_STORAGE_KEY, res.jwt);
            navigate("/profile");
        } catch (err: any) {
            console.error("Login error:", err);
            setError(err.message ?? "Login failed");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <h2>Login</h2>

            <form
                onSubmit={handleSubmit}
                style={{ maxWidth: 400, display: "flex", flexDirection: "column", gap: "0.75rem" }}
            >
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

                <button type="submit" disabled={loading}>
                    {loading ? "Logging in..." : "Log in"}
                </button>
            </form>

            {error && <p style={{ color: "red", marginTop: "0.75rem" }}>{error}</p>}
        </div>
    );
}
