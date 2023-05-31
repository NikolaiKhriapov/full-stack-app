import {Flex, Heading, Image, Link, Stack} from "@chakra-ui/react";
import {useAuth} from "../context/AuthContext.jsx";
import {useNavigate} from "react-router-dom";
import {useEffect} from "react";
import CreateCustomerForm from "../../shared/CreateCustomerForm.jsx";

const SignUp = () => {
    const {customer, setCustomerFromToken} = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        if (customer) {
            navigate("/dashboard");
        }
    })

    return (
        <Stack minH={'100vh'} direction={{base: 'column', md: 'row'}}>
            <Flex p={8} flex={1} align={'center'} justify={'center'}>
                <Stack spacing={4} w={'full'} maxW={'md'}>
                    <Heading fontSize={'2xl'}>Register account</Heading>
                    <CreateCustomerForm
                        onSuccess={(token) => {
                            localStorage.setItem("access_token", token);
                            setCustomerFromToken();
                            navigate("/dashboard");
                        }}
                    />
                    <Link color={"blue.500"} href={"/sign-in"}>
                        Already have an account? Sign in now
                    </Link>
                </Stack>
            </Flex>
            <Flex flex={1}>
                <Image
                    alt={'Sign-In Image'}
                    objectFit={'cover'}
                    src={
                        'https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=1352&q=80'
                    }
                />
            </Flex>
        </Stack>
    );

}

export default SignUp;