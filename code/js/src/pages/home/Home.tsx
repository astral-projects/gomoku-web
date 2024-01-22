import * as React from 'react';
import { useCurrentUserId } from '../GomokuContainer';
import { Navigate } from 'react-router-dom';
import { webRoutes } from '../../App';

export function Home() {
    const user = useCurrentUserId();
    return (
        <div className="home-container">
            {user ? <Navigate to={webRoutes.me} /> : null}
            <h1>Home</h1>
            <h2>Welcome to the Gomoku Game!</h2>
            <p>This is a simple game of computer where you can play against other players.</p>
            <img className="logo" src="/gomoku.png" alt="Gomoku Game Logo" />
            <p>
                This work was done with ❤️ by group 14 of DAW in ISEL:
                <ul>
                    <li>
                        49428 - <a href="https://github.com/FranciscoEngenheiro">Francisco Engenheiro</a>
                    </li>
                    <li>
                        49513 - <a href="https://github.com/Diogofmr">Diogo Rodrigues</a>
                    </li>
                    <li>
                        48666 - <a href="https://github.com/TiagoFrazao01">Tiago Frazão</a>
                    </li>
                </ul>
            </p>
        </div>
    );
}
