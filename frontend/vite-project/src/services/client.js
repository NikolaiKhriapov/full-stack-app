import axios from "axios";

const getAuthConfig = () => ({
    headers: {
        Authorization: `Bearer ${localStorage.getItem("access_token")}`
    }
})

export const getCustomers = async () => {
    try {
        return await axios.get(
            `${import.meta.env.VITE_API_BASE_URL}/api/v1/customers`,
            getAuthConfig()
        )
    } catch (error) {
        throw error;
    }
}

export const createCustomer = async (customer) => {
    try {
        return await axios.post(
            `${import.meta.env.VITE_API_BASE_URL}/api/v1/customers`,
            customer,
            // getAuthConfig()
        )
    } catch (error) {
        throw error;
    }
}

export const updateCustomer = async (customerId, updatedCustomer) => {
    try {
        return await axios.put(
            `${import.meta.env.VITE_API_BASE_URL}/api/v1/customers/${customerId}`,
            updatedCustomer,
            getAuthConfig()
        )
    } catch (error) {
        throw error;
    }
}

export const deleteCustomer = async (customerId) => {
    try {
        return await axios.delete(
            `${import.meta.env.VITE_API_BASE_URL}/api/v1/customers/${customerId}`,
            getAuthConfig()
        )
    } catch (error) {
        throw error;
    }
}

export const signIn = async (usernameAndPassword) => {
    try {
        return await axios.post(
            `${import.meta.env.VITE_API_BASE_URL}/api/v1/auth/login`,
            usernameAndPassword
        )
    } catch (error) {
        throw error;
    }
}