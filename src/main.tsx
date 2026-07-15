import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "App";
import { UserProvider } from "context/user.context";
import { MaterialUIControllerProvider } from "context";

const container = document.getElementById("app");

if (!container) {
  throw new Error("Elemento raiz 'app' não encontrado.");
}

const root = createRoot(container);

root.render(
  <BrowserRouter>
    <MaterialUIControllerProvider>
        <UserProvider>
            <App />
        </UserProvider>
    </MaterialUIControllerProvider>
  </BrowserRouter>
);
