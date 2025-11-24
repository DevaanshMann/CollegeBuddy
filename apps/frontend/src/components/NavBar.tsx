import { Link, useLocation } from "react-router-dom";
import { JWT_STORAGE_KEY } from "../config";
import { useEffect, useState } from "react";

export function NavBar() {
    const location = useLocation();
    const [isAuthed, setIsAuthed] = useState(Boolean(localStorage.getItem(JWT_STORAGE_KEY)));

    // Update auth state when location changes
    useEffect(() => {
        setIsAuthed(Boolean(localStorage.getItem(JWT_STORAGE_KEY)));
    }, [location]);

    const onLogout = () => {
        localStorage.removeItem(JWT_STORAGE_KEY);
        setIsAuthed(false);
        window.location.href = "/";
    };

    const getPillStyle = (path: string) => {
        const isCurrentPage = location.pathname === path;
        return {
            display: "flex",
            alignItems: "center",
            gap: "0.5rem",
            padding: "0.5rem 1rem",
            borderRadius: "999px",
            backgroundColor: isCurrentPage ? "#007bff" : "#f3f4f6",
            color: isCurrentPage ? "white" : "#333",
            textDecoration: "none",
            fontWeight: isCurrentPage ? "bold" : "normal",
            fontSize: "1.1rem",
            transition: "all 0.2s ease",
            border: isCurrentPage ? "2px solid #007bff" : "2px solid transparent",
        } as const;
    };

    const isLoginPage = location.pathname === "/login";
    const isSignupPage = location.pathname === "/signup";
    const isAuthPage = isLoginPage || isSignupPage;

    return (
        <nav
            style={{
                display: "flex",
                alignItems: "center",
                gap: "0.75rem",
                borderBottom: "1px solid #e5e7eb",
                paddingBottom: "1rem",
                marginBottom: "1.5rem",
                flexWrap: "wrap",
            }}
        >
            {!isAuthPage && (
                <span style={{ fontFamily: "'Pacifico', cursive", fontSize: "1.5rem" }}>CollegeBuddy</span>
            )}

            {!isAuthed && (
                <>
                    {!isSignupPage && (
                        <Link to="/signup" style={getPillStyle("/signup")}>
                            Sign Up
                        </Link>
                    )}
                    {!isLoginPage && (
                        <Link to="/login" style={getPillStyle("/login")}>
                            Login
                        </Link>
                    )}
                </>
            )}

            {isAuthed && (
                <>
                    <Link to="/profile" style={getPillStyle("/profile")}>
                        <span>👤</span>
                        <span>Profile</span>
                    </Link>
                    <Link to="/search" style={getPillStyle("/search")}>
                        <span>🔍</span>
                        <span>Search</span>
                    </Link>
                    <Link to="/connections" style={getPillStyle("/connections")}>
                        <span>🤝</span>
                        <span>Connections</span>
                    </Link>

                    <button
                        onClick={onLogout}
                        style={{
                            marginLeft: "auto",
                            display: "flex",
                            alignItems: "center",
                            gap: "0.5rem",
                            padding: "0.5rem 1rem",
                            borderRadius: "999px",
                            backgroundColor: "#fef2f2",
                            border: "2px solid #f97316",
                            color: "#f97316",
                            cursor: "pointer",
                            fontSize: "1.1rem",
                            fontFamily: "inherit",
                        }}
                    >
                        <span>🚪</span>
                        <span>Logout</span>
                    </button>
                </>
            )}
        </nav>
    );
}