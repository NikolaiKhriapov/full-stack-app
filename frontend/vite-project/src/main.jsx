import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'
import SignIn from "./components/signin/SignIn.jsx";
import AuthProvider from "./components/context/AuthContext.jsx";
import {ChakraProvider} from '@chakra-ui/react'
import {createStandaloneToast} from '@chakra-ui/react'
import {createBrowserRouter, RouterProvider} from "react-router-dom";
import './index.css'
import ProtectedRoute from "./shared/ProtectedRoute.js";
import SignUp from "./components/signup/SignUp.jsx";

const {ToastContainer} = createStandaloneToast()

const router = createBrowserRouter([
    {
        path: "/sign-in",
        element: <SignIn/>,
    },
    {
        path: "/sign-up",
        element: <SignUp/>,
    },
    {
        path: "/dashboard",
        element: <ProtectedRoute><App/></ProtectedRoute>
    }
])

ReactDOM
    .createRoot(document.getElementById('root'))
    .render(
        <React.StrictMode>
            <ChakraProvider>
                <AuthProvider>
                    <RouterProvider router={router}/>
                </AuthProvider>
                <ToastContainer/>
            </ChakraProvider>
        </React.StrictMode>,
    )
