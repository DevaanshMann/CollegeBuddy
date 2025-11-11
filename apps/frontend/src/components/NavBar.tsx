import { Link, useNavigate } from "react-router-dom";
import { JWT_STORAGE_KEY } from "../config";

export function NavBar() {
    const navigate = useNavigate();
    const isLoggedIn = !!localStorage.getItem(JWT_STORAGE_KEY);

    function handleLogout() {
        localStorage.removeItem(JWT_STORAGE_KEY);
        navigate("/login");
    }

    return (
        <nav
            style={{
                display: "flex",
                alignItems: "center",
                gap: "1rem",
                padding: "0.75rem 1rem",
                borderBottom: "1px solid #ddd",
            }}
        >
            <span style={{ fontWeight: "bold" }}>CollegeBuddy</span>

            <Link to="/signup">Sign Up</Link>
            <Link to="/login">Login</Link>

            {isLoggedIn && (
                <>
                    <Link to="/profile">Profile</Link>
                    <Link to="/search">Search</Link>
                    <Link to="/connections">Connections</Link>
                </>
            )}

            <div style={{ marginLeft: "auto" }}>
                {isLoggedIn && (
                    <button onClick={handleLogout}>Logout</button>
                )}
            </div>
        </nav>
    );
}
