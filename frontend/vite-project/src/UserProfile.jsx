const UserProfile = ({name, age, gender, imageNumber, ...props}) => {

    return (
        <div>
            <h1>{name}</h1>
            <p>{age}</p>
            <img src={`https://randomuser.me/api/portraits/${gender}/${imageNumber}.jpg`}/>
            {props.children}
        </div>
    );
}

export default UserProfile;
