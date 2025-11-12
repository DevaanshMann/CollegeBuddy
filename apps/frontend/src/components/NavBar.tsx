import { Link, useLocation, useNavigate } from "react-router-dom";
import { JWT_STORAGE_KEY } from "../config";

export function NavBar() {
    const location = useLocation();
    const navigate = useNavigate();
    const token = localStorage.getItem(JWT_STORAGE_KEY);
    const isAuthed = Boolean(token);

    const onLogout = () => {
        localStorage.removeItem(JWT_STORAGE_KEY);
        navigate("/login");
    };

    const isActive = (path: string) =>
        location.pathname === path ? { textDecoration: "underline" } : {};

    return (
        <nav
            style={{
                display: "flex",
                alignItems: "center",
                gap: "1rem",
                borderBottom: "1px solid #374151",
                paddingBottom: "0.75rem",
            }}
        >
            <span style={{ fontWeight: 700, fontSize: "1.2rem" }}>CollegeBuddy</span>

            {!isAuthed && (
                <>
                    <Link to="/signup" style={isActive("/signup")}>
                        Sign Up
                    </Link>
                    <Link to="/login" style={isActive("/login")}>
                        Login
                    </Link>
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
                        }}
                    >
                        Logout
                    </button>
                </>
            )}
        </nav>
    );
}
