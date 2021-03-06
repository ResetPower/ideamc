import { StringMap } from "../tools/types";
import App from "./App";
import { ephConfigs, setConfig } from "./config";
import { darkTheme, lightTheme } from "./global";

const ss = document.documentElement.style;
const html = document.querySelector("html");

export interface EphTheme {
  type: "light" | "dark";
  palette: {
    background: string;
    primary: string;
    secondary: string;
    shallow: string;
    divide: string;
    card: string;
  };
}

export function setProperties(obj: StringMap): void {
  for (const i in obj) {
    ss.setProperty(i, obj[i]);
  }
}

export function applyTheme(theme: EphTheme): void {
  theme.type === "dark"
    ? // add class "dark" if theme type is dark
      html && !html.classList.contains("dark") && html.classList.add("dark")
    : // remove class "dark" if theme type is not dark (light)
      html && html.classList.contains("dark") && html.classList.remove("dark");
  // set values of css variables
  setProperties({
    "--eph-background-color": theme.palette.background,
    "--eph-primary-color": theme.palette.primary,
    "--eph-secondary-color": theme.palette.secondary,
    "--eph-shallow-color": theme.palette.shallow,
    "--eph-divide-color": theme.palette.divide,
    "--eph-card-color": theme.palette.card,
  });
}

export const defineTheme = (theme: EphTheme): EphTheme => theme;

const isDark = window.matchMedia("(prefers-color-scheme: dark)");

// set config according to system
function detectSystemTheme(): void {
  const prefersDarkMode = isDark.matches;
  setConfig(() => (ephConfigs.theme = prefersDarkMode ? "dark" : "light"));
}

export function updateTheme(updateUI = true): void {
  if (ephConfigs.themeFollowOs) {
    detectSystemTheme();
  }
  applyTheme(ephConfigs.theme === "dark" ? darkTheme : lightTheme);
  updateUI && App.updateUI();
}

// listen to system theme changes
isDark.addEventListener("change", () => updateTheme());
