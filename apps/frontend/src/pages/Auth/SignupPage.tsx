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
    const [campusDomain, setCampusDomain] = useState("");
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
            console.error("Signup error:", err);
            setError(err.message ?? "Signup failed");
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
                <h2 style={{ textAlign: "center", marginBottom: "1.5rem" }}>Sign Up</h2>

                <form
                    onSubmit={handleSubmit}
                    style={{ display: "flex", flexDirection: "column", gap: "1rem" }}
                >
                    <div>
                        <label style={{ display: "block", marginBottom: "0.5rem", fontWeight: 500, fontSize: "1.1rem" }}>
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
                                boxSizing: "border-box",
                                fontSize: "1.1rem"
                            }}
                        />
                    </div>

                    <div>
                        <label style={{ display: "block", marginBottom: "0.5rem", fontWeight: 500, fontSize: "1.1rem" }}>
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
                                boxSizing: "border-box",
                                fontSize: "1.1rem"
                            }}
                        />
                    </div>

                    <div>
                        <label style={{ display: "block", marginBottom: "0.5rem", fontWeight: 500, fontSize: "1.1rem" }}>
                            Campus Domain
                        </label>
                        <input
                            type="text"
                            value={campusDomain}
                            onChange={(e) => setCampusDomain(e.target.value)}
                            required
                            style={{
                                width: "100%",
                                padding: "0.75rem",
                                borderRadius: "4px",
                                border: "1px solid #ccc",
                                boxSizing: "border-box",
                                fontSize: "1.1rem"
                            }}
                        />
                    </div>

                    <button
                        type="submit"
                        style={{
                            padding: "0.75rem",
                            backgroundColor: "#4a90d9",
                            color: "#fff",
                            border: "none",
                            borderRadius: "4px",
                            cursor: "pointer",
                            fontWeight: 500,
                            marginTop: "0.5rem"
                        }}
                    >
                        Create account
                    </button>
                </form>

                {message && <p style={{ color: "green", marginTop: "1rem", textAlign: "center" }}>{message}</p>}
                {error && <p style={{ color: "red", marginTop: "1rem", textAlign: "center" }}>{error}</p>}
            </div>
        </div>
    );
}
