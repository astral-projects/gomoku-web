import * as React from 'react';
import { useCurrentUserName } from '../pages/GomokuContainer';
import { Link } from 'react-router-dom';
import { webRoutes } from '../App';
import { Avatar, Divider, IconButton, ListItemIcon, Menu, MenuItem, Tooltip } from '@mui/material';
import PersonAdd from '@mui/icons-material/PersonAdd';
import { Logout } from '@mui/icons-material';
import LoginIcon from '@mui/icons-material/Login';
import MenuIcon from '@mui/icons-material/Menu';
import HomeIcon from '@mui/icons-material/Home';
import FormatListNumberedIcon from '@mui/icons-material/FormatListNumbered';
import InfoIcon from '@mui/icons-material/Info';

export function Navbar() {
    const user = useCurrentUserName();
    const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
    const open = Boolean(anchorEl);
    const handleClick = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };
    const handleClose = () => {
        setAnchorEl(null);
    };

    return (
        <React.Fragment>
            <nav>
                {user ? (
                    <Link to={`${webRoutes.me}`}>
                        <img src="/gomoku.png" alt="Gomoku Logo" />
                    </Link>
                ) : (
                    <Link to={`${webRoutes.home}`}>
                        <img src="/gomoku.png" alt="Gomoku Logo" />
                    </Link>
                )}

                <h1>Gomoku Royale</h1>
                <Tooltip title="Menu">
                    <IconButton
                        onClick={handleClick}
                        size="large"
                        aria-controls={open ? 'account-menu' : undefined}
                        aria-haspopup="true"
                        aria-expanded={open ? 'true' : undefined}
                        sx={{ ml: 2, justifySelf: 'flex-end', width: '5rem', height: '5rem' }}
                    >
                        <Avatar>
                            <MenuIcon />
                        </Avatar>
                    </IconButton>
                </Tooltip>
                <Menu
                    anchorEl={anchorEl}
                    id="account-menu"
                    open={open}
                    onClose={handleClose}
                    onClick={handleClose}
                    PaperProps={{
                        elevation: 0,
                        sx: {
                            overflow: 'visible',
                            filter: 'drop-shadow(0px 2px 8px rgba(0,0,0,0.32))',
                            mt: 1.5,
                            '& .MuiAvatar-root': {
                                width: 32,
                                height: 32,
                                ml: -0.5,
                                mr: 1,
                            },
                            '&::before': {
                                content: '""',
                                display: 'block',
                                position: 'absolute',
                                top: 0,
                                right: 14,
                                width: 10,
                                height: 10,
                                bgcolor: 'background.paper',
                                transform: 'translateY(-50%) rotate(45deg)',
                                zIndex: 0,
                            },
                        },
                    }}
                    transformOrigin={{ horizontal: 'right', vertical: 'top' }}
                    anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
                >
                    <MenuItem>
                        <ListItemIcon>
                            <HomeIcon fontSize="small" />
                        </ListItemIcon>
                        {user ? <Link to={`${webRoutes.me}`}>Home</Link> : <Link to={`${webRoutes.home}`}>Home</Link>}
                    </MenuItem>
                    <MenuItem>
                        <ListItemIcon>
                            <FormatListNumberedIcon fontSize="small" />
                        </ListItemIcon>
                        <Link to={`${webRoutes.rankings}`}>Rankings</Link>
                    </MenuItem>
                    <MenuItem>
                        <ListItemIcon>
                            <InfoIcon fontSize="small" />
                        </ListItemIcon>
                        <Link to={`${webRoutes.about}`}>About</Link>
                    </MenuItem>
                    <Divider />
                    <MenuItem>
                        <ListItemIcon>
                            {user ? <Logout fontSize="small" /> : <LoginIcon fontSize="small" />}
                        </ListItemIcon>
                        {user ? (
                            <Link to={`${webRoutes.logout}`}>Logout</Link>
                        ) : (
                            <Link to={`${webRoutes.login}`}>Login</Link>
                        )}
                    </MenuItem>
                    {user ? null : (
                        <MenuItem>
                            <ListItemIcon>
                                <PersonAdd fontSize="small" />
                            </ListItemIcon>
                            <Link to={`${webRoutes.register}`}>Sign Up</Link>
                        </MenuItem>
                    )}
                </Menu>
            </nav>
        </React.Fragment>
    );
}
