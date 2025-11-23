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

    const isActive = (path: string) =>
        location.pathname === path ? { textDecoration: "underline", fontWeight: "bold" } : {};

    const isLoginPage = location.pathname === "/login";
    const isSignupPage = location.pathname === "/signup";
    const isAuthPage = isLoginPage || isSignupPage;

    return (
        <nav
            style={{
                display: "flex",
                alignItems: "center",
                gap: "1rem",
                borderBottom: "1px solid #374151",
                paddingBottom: "0.75rem",
                marginBottom: "1rem",
            }}
        >
            {!isAuthPage && (
                <span style={{ fontFamily: "'Pacifico', cursive", fontSize: "1.5rem" }}>CollegeBuddy</span>
            )}

            {!isAuthed && (
                <>
                    {!isSignupPage && (
                        <Link to="/signup" style={isActive("/signup")}>
                            Sign Up
                        </Link>
                    )}
                    {!isLoginPage && (
                        <Link to="/login" style={isActive("/login")}>
                            Login
                        </Link>
                    )}
                </>
            )}

            {isAuthed && (
                <>
                    <Link to="/profile" style={isActive("/profile")}>
                        Profile
                    </Link>
                    <Link to="/search" style={isActive("/search")}>
                        Search
                    </Link>
                    <Link to="/connections" style={isActive("/connections")}>
                        Connections
                    </Link>

                    <button
                        onClick={onLogout}
                        style={{
                            marginLeft: "auto",
                            background: "transparent",
                            border: "none",
                            color: "#f97316",
                            cursor: "pointer",
                            fontSize: "1rem",
                        }}
                    >
                        Logout
                    </button>
                </>
            )}
        </nav>
    );
}