import {createContext, useContext, useEffect, useState} from "react";
import {signIn as performSignIn} from "../../services/client.js";
import jwtDecode from "jwt-decode";

const AuthContext = createContext({});

const AuthProvider = ({children}) => {

    const [customer, setCustomer] = useState(null);

    const setCustomerFromToken = () => {
        let token = localStorage.getItem("access_token");
        if (token) {
            token = jwtDecode(token);
            setCustomer({
                username: token.sub,
                roles: token.scopes
            })
        }
    }

    useEffect(() => {
        setCustomerFromToken();
    }, [])

    const signIn = async (usernameAndPassword) => {
        return new Promise((resolve, reject) => {
            performSignIn(usernameAndPassword).then(response => {
                const jwtToken = response.headers["authorization"];
                localStorage.setItem("access_token", jwtToken);

                const decodedToken = jwtDecode(jwtToken);

                setCustomer({
                    username: decodedToken.sub,
                    roles: decodedToken.scopes
                })
                resolve(response);
            }).catch(error => {
                reject(error);
            })
        })
    }

    const signOut = () => {
        localStorage.removeItem("access_token");
        setCustomer(null);
    }

    const isCustomerAuthenticated = () => {
        const token = localStorage.getItem("access_token");
        if (!token) {
            return false;
        }
        const {exp: expiration} = jwtDecode(token);
        if (Date.now() > expiration * 1000) {
            signOut();
            return false;
        }
        return true;
    }

    return (
        <AuthContext.Provider value={{
            customer,
            signIn,
            signOut,
            isCustomerAuthenticated,
            setCustomerFromToken
        }}>
            {children}
        </AuthContext.Provider>
    )
}

export const useAuth = () => useContext(AuthContext);
export default AuthProvider;