import type { FormEvent } from "react";
import { useState } from "react";
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

    async function handleSubmit(e: FormEvent) {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            const res = await apiClient.post<LoginResponse>("/auth/login", {
                email,
                password,
            });

            if (res.jwt) {
                localStorage.setItem(JWT_STORAGE_KEY, res.jwt);

                // Force a full page reload to update auth state
                window.location.href = "/profile";
            } else {
                setError("Login failed - no token received");
            }
        } catch (err: any) {
            console.error("Login error:", err);
            setError(err.message ?? "Login failed");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div style={{
            display: "flex",
            flexDirection: "column",
            justifyContent: "center",
            alignItems: "center",
            minHeight: "80vh",
            padding: "1rem"
        }}>
            <h1 style={{
                fontFamily: "'Pacifico', cursive",
                fontSize: "3rem",
                marginBottom: "1.5rem",
                color: "#333"
            }}>
                CollegeBuddy
            </h1>
            <div style={{
                backgroundColor: "#fff",
                padding: "2rem",
                borderRadius: "8px",
                boxShadow: "0 2px 10px rgba(0, 0, 0, 0.1)",
                width: "100%",
                maxWidth: "400px"
            }}>
                <h2 style={{ textAlign: "center", marginBottom: "1.5rem" }}>Login</h2>

                <form
                    onSubmit={handleSubmit}
                    style={{ display: "flex", flexDirection: "column", gap: "1rem" }}
                >
                    <div>
                        <label style={{ display: "block", marginBottom: "0.5rem", fontWeight: 500 }}>
                            Email (.edu)
                        </label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                            style={{
                                width: "100%",
                                padding: "0.75rem",
                                borderRadius: "4px",
                                border: "1px solid #ccc",
                                boxSizing: "border-box"
                            }}
                        />
                    </div>

                    <div>
                        <label style={{ display: "block", marginBottom: "0.5rem", fontWeight: 500 }}>
                            Password
                        </label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            style={{
                                width: "100%",
                                padding: "0.75rem",
                                borderRadius: "4px",
                                border: "1px solid #ccc",
                                boxSizing: "border-box"
                            }}
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        style={{
                            padding: "0.75rem",
                            backgroundColor: "#4a90d9",
                            color: "#fff",
                            border: "none",
                            borderRadius: "4px",
                            cursor: loading ? "not-allowed" : "pointer",
                            fontWeight: 500,
                            marginTop: "0.5rem"
                        }}
                    >
                        {loading ? "Logging in..." : "Log in"}
                    </button>
                </form>

                {error && <p style={{ color: "red", marginTop: "1rem", textAlign: "center" }}>{error}</p>}
            </div>
        </div>
    );
}