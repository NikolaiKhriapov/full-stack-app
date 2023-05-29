import {useEffect} from "react";
import {useAuth} from "../components/context/AuthContext.jsx";
import {useNavigate} from "react-router-dom";

const ProtectedRoute = ({children}) => {
    const {isCustomerAuthenticated} = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        if (!isCustomerAuthenticated()) {
            navigate("/");
        }
    }, [isCustomerAuthenticated]);

    return isCustomerAuthenticated() ? children : null;
};

export default ProtectedRoute;
