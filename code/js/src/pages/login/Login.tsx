import * as React from 'react';
import { useReducer } from 'react';
import { Link, Navigate, useLocation } from 'react-router-dom';
import { login } from '../../services/usersService';
import { ProblemModel } from '../../services/media/ProblemModel';
import { LoginOutput } from '../../services/models/users/LoginOuputModel';
import { Id, User, Username } from '../../domain/User';
import { Entity } from '../../services/media/siren/Entity';
import { useCurrentUserId, useSetUserId, useSetUserName } from '../GomokuContainer';
import { logUnexpectedAction } from '../utils/logUnexpetedAction';
import { isSuccessful } from '../utils/responseData';
import { webRoutes } from '../../App';
import FaceSharpIcon from '@mui/icons-material/FaceSharp';
import LockIcon from '@mui/icons-material/Lock';
import { SocialIcon } from 'react-social-icons';

type State =
    | { tag: 'editing'; error?: string; inputs: { username: string; password: string } }
    | { tag: 'submitting'; username: string; password: string }
    | { tag: 'redirect' };

type Action =
    | { type: 'edit'; inputName: string; inputValue: string }
    | { type: 'submit' }
    | { type: 'error'; message: string }
    | { type: 'success' };

function reduce(state: State, action: Action): State {
    switch (state.tag) {
        case 'editing':
            if (action.type === 'edit') {
                return {
                    tag: 'editing',
                    error: undefined,
                    inputs: { ...state.inputs, [action.inputName]: action.inputValue },
                };
            } else if (action.type === 'submit') {
                return { tag: 'submitting', username: state.inputs.username, password: state.inputs.password };
            } else {
                logUnexpectedAction(state, action);
                return state;
            }

        case 'submitting':
            if (action.type === 'success') {
                return { tag: 'redirect' };
            } else if (action.type === 'error') {
                return { tag: 'editing', error: action.message, inputs: { username: state.username, password: '' } };
            } else {
                logUnexpectedAction(state, action);
                return state;
            }

        case 'redirect':
            logUnexpectedAction(state, action);
            return state;
    }
}

export function Login() {
    const [state, dispatch] = useReducer(reduce, { tag: 'editing', inputs: { username: '', password: '' } });
    const setUserId = useSetUserId();
    const setUserName = useSetUserName();
    const userId = useCurrentUserId();
    const location = useLocation();

    if (state.tag === 'redirect' || userId !== undefined){
        return <Navigate to={location.state?.source?.pathname || webRoutes.me} replace={true} />;
    }

    function handleChange(ev: React.FormEvent<HTMLInputElement>) {
        dispatch({ type: 'edit', inputName: ev.currentTarget.name, inputValue: ev.currentTarget.value });
    }

    function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault();
        if (state.tag !== 'editing') {
            return;
        }

        dispatch({ type: 'submit' });
        login({ username: state.inputs.username, password: state.inputs.password })
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
                    dispatch({ type: 'success' });
                }
            })
            .catch((err: { message: string }) => {
                dispatch({ type: 'error', message: err.message });
            });
    }

    const oauthGoogleParams = {
        client_id: '908474914470-j0341q2dl8su0ubc2rt1ab0joqcutlo5.apps.googleusercontent.com',
        state: 'state_parameter_passthrough_value',
        redirect_uri: 'http://localhost:4000/googlecallback',
        response_type: 'code',
        scope: 'openid profile email',
    };

    const googleAuthUrl = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${oauthGoogleParams.client_id}&state=${oauthGoogleParams.state}&redirect_uri=${oauthGoogleParams.redirect_uri}&response_type=${oauthGoogleParams.response_type}&scope=${oauthGoogleParams.scope}`;

    const username = state.tag === 'submitting' ? state.username : state.inputs.username;
    const password = state.tag === 'submitting' ? '' : state.inputs.password;
    return (
        <div className="container-login">
            <form className="login-form" onSubmit={handleSubmit}>
                <h2>Login</h2>
                <label className="top-label">Username</label>
                <div className="login-input">
                    <label htmlFor="username">
                        <FaceSharpIcon fontSize="small" />
                    </label>
                    <input
                        id="username"
                        type="text"
                        name="username"
                        value={username}
                        onChange={handleChange}
                        placeholder="Username"
                        required
                    />
                </div>
                <label className="top-label">Password</label>
                <div className="login-input">
                    <label htmlFor="password">
                        <LockIcon fontSize="medium" />
                    </label>
                    <input
                        id="password"
                        type="password"
                        name="password"
                        value={password}
                        onChange={handleChange}
                        placeholder="********"
                        required
                    />
                </div>
                <p>Forgot Password?</p>
                <div className="btn-login">
                    <button type="submit">Login</button>
                </div>
                <div className="sign-in-with">
                    <p>Or Sign Up Using</p>
                    <div className="login-icons">
                        <i className="fab fa-facebook-f">
                            <span>
                                <SocialIcon url="www.facebook.com" />
                            </span>
                        </i>
                        <i className="fab fa-google">
                            <span>
                                <SocialIcon url={googleAuthUrl} />
                            </span>
                        </i>
                        <i className="fab fa-github">
                            <span>
                                <SocialIcon url="www.github.com" />
                            </span>
                        </i>
                    </div>
                </div>
                <div className="login-text">
                    Dont have an account? <Link to={webRoutes.register}>Sign Up</Link>
                </div>
                {state.tag === 'editing' && state.error}
            </form>
        </div>
    );
}
