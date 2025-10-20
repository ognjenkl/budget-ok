import { App as AntdApp } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import './App.css';
import EnvelopesTable from "./components/EnvelopesTable/EnvelopesTable";

const queryClient = new QueryClient();

function AppContent() {
  return (
    <div className="app-container">
      <h1>Budget OK</h1>
      <div className="card envelopes-card">
        <EnvelopesTable />
      </div>
    </div>
  );
}

export default function App() {
  return (
      <QueryClientProvider client={queryClient}>
        <AntdApp>
          <AppContent/>
        </AntdApp>
      </QueryClientProvider>
  );
}
