import { createTheme } from '@mui/material/styles';

/**
 * Tema de Material-UI configurado para usar fuentes del sistema
 * en lugar de cargar Roboto desde Google Fonts
 */
const theme = createTheme({
  typography: {
    fontFamily: [
      '-apple-system',
      'BlinkMacSystemFont',
      '"Segoe UI"',
      'Roboto',
      '"Helvetica Neue"',
      'Arial',
      'sans-serif',
      '"Apple Color Emoji"',
      '"Segoe UI Emoji"',
      '"Segoe UI Symbol"',
    ].join(','),
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        '@global': {
          '@font-face': [],
        },
      },
    },
  },
});

export default theme;

