import Cookies from 'js-cookie';
import * as React from 'react';
import { useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import { webRoutes } from '../App';
import { login, register } from '../services/usersService';
import { isSuccessful } from '../pages/utils/responseData';
import { ProblemModel } from '../services/media/ProblemModel';
import { useSetUserId, useSetUserName } from '../pages/GomokuContainer';
import { LoginOutput } from '../services/models/users/LoginOuputModel';
import { Entity } from '../services/media/siren/Entity';
import { Id, User, Username } from '../domain/User';
import { RegisterOutput } from '../services/models/users/RegisterOuputModel';

const GOOGLE_COOKIE_NAME = '_google_username';
const GOOGLE_EMAIL_COOKIE_NAME = '_google_email';

/** generate a random password (string) with the following requirements:
    - at least 8 characters
    - at least one uppercase letter
    - at least one lowercase letter
    - at least one number
    */
const generateRandomValidPassword = () => {
    const validChars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    const validCharsLength = validChars.length;
    let password = '';
    while (password.length < 10) {
        password += validChars.charAt(Math.floor(Math.random() * validCharsLength));
    }
    return password;
};

type State =
    | { tag: 'loading' }
    | { tag: 'redirectToregister' }
    | { tag: 'redirectTologin' }
    | { tag: 'redirect' }
    | { tag: 'error'; message: string };

type Action = { type: 'success' } | { type: 'error'; message: string } | { type: 'newUser' } | { type: 'existingUser' };

function reduce(state: State, action: Action): State {
    switch (state.tag) {
        case 'loading':
            if (action.type === 'success') {
                return { tag: 'redirect' };
            } else if (action.type === 'error') {
                return { tag: 'error', message: action.message };
            } else if (action.type === 'newUser') {
                return { tag: 'redirectToregister' };
            } else if (action.type === 'existingUser') {
                return { tag: 'redirectTologin' };
            } else {
                console.log('Unexpected action');
                return state;
            }
        case 'redirect':
            console.log('Unexpected action');
            return state;
        case 'redirectToregister':
            if (action.type === 'success') {
                return { tag: 'redirectTologin' };
            } else if (action.type === 'error') {
                return { tag: 'error', message: action.message };
            }
            return state;
        case 'redirectTologin':
            if (action.type === 'success') {
                return { tag: 'redirect' };
            } else if (action.type === 'error') {
                return { tag: 'error', message: action.message };
            }
            return state;
    }
}

export function GoogleCallback() {
    const [state, dispatch] = React.useReducer(reduce, { tag: 'loading' });
    const setUserId = useSetUserId();
    const setUserName = useSetUserName();
    const password = generateRandomValidPassword();
    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        // get the code from the query params
        console.log(params);
        const code = params.get('code');
        // const state = params.get('state');
        // TODO: check state
        console.log('here');

        if (state.tag === 'loading') {
            const fetchToken = async () =>
                await fetch(`https://www.googleapis.com/oauth2/v3/token`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        code: code,
                        client_id: '908474914470-j0341q2dl8su0ubc2rt1ab0joqcutlo5.apps.googleusercontent.com',
                        client_secret: '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',
                        redirect_uri: 'http://localhost:4000/googlecallback',
                        grant_type: 'authorization_code',
                    }),
                });

            try {
                const tokenResponse = fetchToken();
                if (tokenResponse) {
                    tokenResponse.then(async response => {
                        const token = await response.json();
                        const accessToken = token.access_token;
                        const fetchUser = async () =>
                            await fetch(
                                `https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=${accessToken}`,
                                {
                                    method: 'GET',
                                    headers: {
                                        'Content-Type': 'application/json',
                                    },
                                }
                            );
                        const userResponse = fetchUser();
                        if (userResponse) {
                            userResponse.then(async response => {
                                const user = await response.json();
                                const username = user.name;
                                const email = user.email;
                                const googleUsername = Cookies.get(GOOGLE_COOKIE_NAME);
                                const googleEmail = Cookies.get(GOOGLE_EMAIL_COOKIE_NAME);
                                if (googleUsername === undefined && googleEmail === undefined) {
                                    Cookies.set(GOOGLE_COOKIE_NAME, username);
                                    Cookies.set(GOOGLE_EMAIL_COOKIE_NAME, email);
                                    dispatch({ type: 'newUser' });
                                } else {
                                    dispatch({ type: 'existingUser' });
                                }
                            });
                        }
                    });
                }
            } catch (error) {
                dispatch({ type: 'error', message: error });
                console.log(error);
            }
        }

        if (state.tag === 'redirectToregister') {
            register({
                username: Cookies.get(GOOGLE_COOKIE_NAME),
                email: Cookies.get(GOOGLE_EMAIL_COOKIE_NAME),
                password: password,
            })
                .then(result => {
                    if (!isSuccessful(result.contentType)) {
                        const errorData = result.json as ProblemModel;
                        dispatch({ type: 'error', message: errorData.detail });
                    } else {
                        const successData = result.json as RegisterOutput;
                        const propeties = successData.properties;
                        const id = propeties.id;
                        setUserId(id);
                        setUserName(Cookies.get(GOOGLE_COOKIE_NAME));
                        Cookies.remove(GOOGLE_COOKIE_NAME);
                        Cookies.remove(GOOGLE_EMAIL_COOKIE_NAME);
                        dispatch({ type: 'success' });
                    }
                })
                .catch(error => {
                    console.log(`Error: ${error}`);
                    dispatch({ type: 'error', message: error });
                });
        }

        if (state.tag === 'redirectTologin') {
            login({ username: Cookies.get(GOOGLE_COOKIE_NAME), password: password })
                .then(result => {
                    if (!isSuccessful(result.contentType)) {
                        const errorData = result.json as ProblemModel;
                        dispatch({ type: 'error', message: errorData.detail });
                    } else {
                        const successData = result.json as LoginOutput;
                        const properties = successData.entities[0] as Entity<User>;
                        const id = properties.properties.id as Id;
                        const username = properties.properties.username as Username;
                        setUserId(id.value);
                        setUserName(username.value);
                        Cookies.remove(GOOGLE_COOKIE_NAME);
                        Cookies.remove(GOOGLE_EMAIL_COOKIE_NAME);
                        dispatch({ type: 'success' });
                    }
                })
                .catch(error => {
                    console.log(`Error: ${error}`);
                    dispatch({ type: 'error', message: error });
                });
        }
    }, [setUserName, setUserId, state.tag, password]);

    switch (state.tag) {
        case 'redirect':
            return <Navigate to={webRoutes.me} replace={true} />;
        case 'error':
            return <Navigate to={webRoutes.login} replace={true} />;
        case 'loading':
            return <div>Loading...</div>;
    }
}
