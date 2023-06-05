import {Formik, Form, useField} from 'formik';
import * as Yup from 'yup';
import {Alert, AlertIcon, Box, Button, FormLabel, Input, Stack, VStack, Image} from "@chakra-ui/react";
import {customerProfilePictureUrl, updateCustomer, updateCustomerProfileImage} from "../../services/client.js";
import {successNotification, errorNotification} from "../../services/notification.js";
import {useCallback} from "react";
import {useDropzone} from "react-dropzone";

const MyTextInput = ({label, ...props}) => {
    // useField() returns [formik.getFieldProps(), formik.getFieldMeta()]
    // which we can spread on <input>. We can use field meta to show an error
    // message if the field is invalid and it has been touched (i.e. visited)
    const [field, meta] = useField(props);
    return (
        <Box>
            <FormLabel htmlFor={props.id || props.name}>{label}</FormLabel>
            <Input className="text-input" {...field} {...props} />
            {meta.touched && meta.error ? (
                <Alert className="error" status={"error"} mt={2}>
                    <AlertIcon/>
                    {meta.error}
                </Alert>
            ) : null}
        </Box>
    );
};

// const MySelect = ({label, ...props}) => {
//     const [field, meta] = useField(props);
//     return (
//         <Box>
//             <FormLabel htmlFor={props.id || props.name}>{label}</FormLabel>
//             <Select {...field} {...props} />
//             {meta.touched && meta.error ? (
//                 <Alert className="error" status={"error"} mt={2}>
//                     <AlertIcon/>
//                     {meta.error}
//                 </Alert>
//             ) : null}
//         </Box>
//     );
// };

const MyDropzone = ({customerId, fetchCustomers}) => {
    const onDrop = useCallback(acceptedFiles => {
        const file = new FormData();
        file.append("file", acceptedFiles[0])

        updateCustomerProfileImage(customerId, file)
            .then(() => {
                successNotification("Success", "Profile image uploaded")
                fetchCustomers();
            }).catch(() => {
                errorNotification("Error", "Profile image failed to upload")
            }
        )
    }, [])
    const {getRootProps, getInputProps, isDragActive} = useDropzone({onDrop})

    return (
        <Box {...getRootProps()}
             w={'100%'}
             textAlign={'center'}
             border={'dashed'}
             borderColor={'gray.200'}
             borderRadius={'3xl'}
             p={6}
             rounded={'md'}
        >
            <input {...getInputProps()} />
            {
                isDragActive ?
                    <p>Drop the image here ...</p> :
                    <p>Drag 'n' drop image here, or click to select image</p>
            }
        </Box>
    )
}

const UpdateCustomerForm = ({fetchCustomers, initialValues, customerId}) => {
    return (
        <>
            <VStack spacing={'5'} mb={'5'}>
                <Image
                    borderRadius={'full'}
                    boxSize={'150px'}
                    objectFit={'cover'}
                    src={customerProfilePictureUrl(customerId)}
                />
                <MyDropzone
                    customerId={customerId}
                    fetchCustomers={fetchCustomers}
                />
            </VStack>
            <Formik
                initialValues={initialValues}
                validationSchema={Yup.object({
                    name: Yup.string()
                        .max(15, 'Must be 15 characters or less')
                        .required('Required'),
                    email: Yup.string()
                        .email('Invalid email address')
                        .required('Required'),
                    age: Yup.number()
                        .min(16, 'Must be at least 16 years of age')
                        .max(100, 'Must be less than 100 years of age')
                        .required('Required'),
                    // gender: Yup.string()
                    //     .oneOf(
                    //         ['MALE', 'FEMALE'],
                    //         'Invalid gender'
                    //     )
                    //     .required('Required'),
                })}
                onSubmit={(updatedCustomer, {setSubmitting}) => {
                    setSubmitting(true);
                    updateCustomer(customerId, updatedCustomer)
                        .then((response) => {
                            console.log(response)
                            successNotification(
                                "Customer updated",
                                `${updatedCustomer.name} was successfully updated`
                            )
                            fetchCustomers();
                        }).catch((error) => {
                        console.log(error)
                        errorNotification(
                            error.code,
                            error.response.data.message
                        )

                    }).finally(() => {
                        setSubmitting(false);
                    })
                }}
            >
                {({isValid, isSubmitting, dirty}) => (
                    <Form>
                        <Stack spacing={"24px"}>
                            <MyTextInput
                                label="Name"
                                name="name"
                                type="text"
                                placeholder="Jane"
                            />

                            <MyTextInput
                                label="Email Address"
                                name="email"
                                type="email"
                                placeholder="jane@formik.com"
                            />

                            <MyTextInput
                                label="Age"
                                name="age"
                                type="number"
                                placeholder="24"
                            />

                            {/*<MySelect label="Gender" name="gender">*/}
                            {/*    <option value="">Select gender</option>*/}
                            {/*    <option value="MALE">Male</option>*/}
                            {/*    <option value="FEMALE">Female</option>*/}
                            {/*</MySelect>*/}

                            <Button isDisabled={!(isValid && dirty) || isSubmitting} type="submit">Submit</Button>
                        </Stack>
                    </Form>
                )}
            </Formik>
        </>
    );
};

export default UpdateCustomerForm;