import { Dispatch, ReactNode, createContext, useContext, useMemo, useReducer } from "react";

type Layout = "dashboard" | "page";
type SidenavColor = "primary" | "secondary" | "info" | "success" | "warning" | "error" | "dark";

type ControllerState = {
  miniSidenav: boolean;
  direction: "ltr" | "rtl";
  layout: Layout;
  sidenavColor: SidenavColor;
  darkMode: boolean;
};

type ControllerAction =
  | { type: "MINI_SIDENAV"; value: boolean }
  | { type: "LAYOUT"; value: Layout }
  | { type: "DIRECTION"; value: "ltr" | "rtl" }
  | { type: "SIDENAV_COLOR"; value: SidenavColor }
  | { type: "DARK_MODE"; value: boolean };

const initialState: ControllerState = {
  miniSidenav: false,
  direction: "ltr",
  layout: "dashboard",
  sidenavColor: "info",
  darkMode: false,
};

const MaterialUI = createContext<[ControllerState, Dispatch<ControllerAction>] | undefined>(undefined);

function reducer(state: ControllerState, action: ControllerAction): ControllerState {
  switch (action.type) {
    case "MINI_SIDENAV":
      return { ...state, miniSidenav: action.value };
    case "LAYOUT":
      return { ...state, layout: action.value };
    case "DIRECTION":
      return { ...state, direction: action.value };
    case "SIDENAV_COLOR":
      return { ...state, sidenavColor: action.value };
    case "DARK_MODE":
      return { ...state, darkMode: action.value };
    default:
      return state;
  }
}

export function MaterialUIControllerProvider({ children }: { children: ReactNode }) {
  const [controller, dispatch] = useReducer(reducer, initialState);
  const value = useMemo<[ControllerState, Dispatch<ControllerAction>]>(
    () => [controller, dispatch],
    [controller]
  );

  return <MaterialUI.Provider value={value}>{children}</MaterialUI.Provider>;
}

export function useMaterialUIController() {
  const context = useContext(MaterialUI);

  if (!context) {
    throw new Error("useMaterialUIController deve ser usado dentro de MaterialUIControllerProvider.");
  }

  return context;
}

export const setMiniSidenav = (dispatch: Dispatch<ControllerAction>, value: boolean) =>
  dispatch({ type: "MINI_SIDENAV", value });

export const setLayout = (dispatch: Dispatch<ControllerAction>, value: Layout) =>
  dispatch({ type: "LAYOUT", value });

export const setDirection = (dispatch: Dispatch<ControllerAction>, value: "ltr" | "rtl") =>
  dispatch({ type: "DIRECTION", value });

export const setSidenavColor = (dispatch: Dispatch<ControllerAction>, value: SidenavColor) =>
  dispatch({ type: "SIDENAV_COLOR", value });

export const setDarkMode = (dispatch: Dispatch<ControllerAction>, value: boolean) =>
  dispatch({ type: "DARK_MODE", value });
