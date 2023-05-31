import React from 'react'
import ReactDOM from 'react-dom/client'
import Customers from './Customer.jsx'
import SignIn from "./components/signin/SignIn.jsx";
import AuthProvider from "./components/context/AuthContext.jsx";
import {ChakraProvider} from '@chakra-ui/react'
import {createStandaloneToast} from '@chakra-ui/react'
import {createBrowserRouter, RouterProvider} from "react-router-dom";
import ProtectedRoute from "./shared/ProtectedRoute.js";
import SignUp from "./components/signup/SignUp.jsx";
import Home from "./Home.jsx";
import Settings from "./Settings.jsx";
import './index.css'

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
        element: <ProtectedRoute><Home/></ProtectedRoute>
    },
    {
        path: "/customers",
        element: <ProtectedRoute><Customers/></ProtectedRoute>
    },
    {
        path: "/settings",
        element: <ProtectedRoute><Settings/></ProtectedRoute>
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
