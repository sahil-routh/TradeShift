import React, { useState, useEffect, useCallback } from 'react';
import { ArrowUpRight, ArrowDownRight, RefreshCw, Filter, Download, History, ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import useAxios from '../hooks/useAxios';

interface Transaction {
  id: number;
  symbol: string;
  transactionType: 'BUY' | 'SELL';
  quantity: number;
  price: number;
  timestamp: string;
}

interface TransactionStats {
  totalTransactions: number;
  buyCount: number;
  sellCount: number;
  totalBuyValue: number;
  totalSellValue: number;
  netValue: number;
}

const TransactionHistory: React.FC = () => {
  const api = useAxios();
  const navigate = useNavigate();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filterType, setFilterType] = useState<'ALL' | 'BUY' | 'SELL'>('ALL');
  const [sortOrder, setSortOrder] = useState<'DESC' | 'ASC'>('DESC');

  // Fetch transactions from backend
  const fetchTransactions = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.get('/api/transactions');
      setTransactions(response.data);
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Failed to fetch transactions';
      setError(msg);
      console.error('Error fetching transactions:', err);
    } finally {
      setLoading(false);
    }
  }, [api]);

  useEffect(() => {
    fetchTransactions();
  }, [fetchTransactions]);

  // Format date
  const formatDate = (timestamp: string): string => {
    const date = new Date(timestamp);
    return date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  // Format currency
  const formatCurrency = (amount: number): string => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  };

  // Filter and sort transactions
  const getFilteredTransactions = (): Transaction[] => {
    let filtered = [...transactions];

    if (filterType !== 'ALL') {
      filtered = filtered.filter(t => t.transactionType === filterType);
    }

    filtered.sort((a, b) => {
      const dateA = new Date(a.timestamp);
      const dateB = new Date(b.timestamp);
      return sortOrder === 'DESC' ? dateB.getTime() - dateA.getTime() : dateA.getTime() - dateB.getTime();
    });

    return filtered;
  };

  // Calculate statistics
  const getStats = (): TransactionStats => {
    const buyTransactions = transactions.filter(t => t.transactionType === 'BUY');
    const sellTransactions = transactions.filter(t => t.transactionType === 'SELL');

    const totalBuyValue = buyTransactions.reduce((sum, t) => sum + (t.price * t.quantity), 0);
    const totalSellValue = sellTransactions.reduce((sum, t) => sum + (t.price * t.quantity), 0);

    return {
      totalTransactions: transactions.length,
      buyCount: buyTransactions.length,
      sellCount: sellTransactions.length,
      totalBuyValue,
      totalSellValue,
      netValue: totalSellValue - totalBuyValue
    };
  };

  const stats = getStats();
  const filteredTransactions = getFilteredTransactions();

  // Export to CSV
  const exportToCSV = () => {
    const headers = ['ID', 'Symbol', 'Type', 'Quantity', 'Price', 'Total', 'Timestamp'];
    const rows = filteredTransactions.map(t => [
      t.id, // Keeping the real database ID for export integrity
      t.symbol,
      t.transactionType,
      t.quantity,
      t.price,
      (t.price * t.quantity).toFixed(2),
      t.timestamp
    ]);

    const csvContent = [
      headers.join(','),
      ...rows.map(row => row.join(','))
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `transactions_${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center p-8 bg-white rounded-xl shadow-lg">
          <RefreshCw className="animate-spin h-8 w-8 text-indigo-600 mx-auto" />
          <span className="mt-3 block text-gray-600">Loading transactions...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center p-8 bg-white rounded-xl shadow-lg">
          <p className="text-red-600 mb-4 font-semibold">{error}</p>
          <button
            onClick={fetchTransactions}
            className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 p-4 sm:p-8 font-['Inter']">

      <header className="mb-8 flex items-center justify-between">
        <button
          onClick={() => navigate('/Dashboard')}
          className="flex items-center text-gray-600 hover:text-indigo-600 transition font-semibold"
        >
          <ArrowLeft className="w-5 h-5 mr-2" />
          Back to Dashboard
        </button>
        <h1 className="text-3xl font-extrabold text-gray-900 flex items-center">
            <History className="w-6 h-6 mr-3 text-indigo-600" />
            Transaction History
        </h1>
        <div>{/* Spacer for alignment */}</div>
      </header>

      <div className="space-y-6">
        {/* Statistics Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="bg-white rounded-xl shadow-md border border-gray-100 p-6">
            <div className="text-sm font-semibold text-gray-500 uppercase mb-1">Total Transactions</div>
            <div className="text-3xl font-extrabold text-gray-900">{stats.totalTransactions}</div>
          </div>

          <div className="bg-white rounded-xl shadow-md border border-gray-100 p-6">
            <div className="text-sm font-semibold text-gray-500 uppercase mb-1">Buy Transactions</div>
            <div className="text-3xl font-extrabold text-green-600">{stats.buyCount}</div>
            <div className="text-xs text-gray-400 mt-1">{formatCurrency(stats.totalBuyValue)}</div>
          </div>

          <div className="bg-white rounded-xl shadow-md border border-gray-100 p-6">
            <div className="text-sm font-semibold text-gray-500 uppercase mb-1">Sell Transactions</div>
            <div className="text-3xl font-extrabold text-red-600">{stats.sellCount}</div>
            <div className="text-xs text-gray-400 mt-1">{formatCurrency(stats.totalSellValue)}</div>
          </div>

          <div className="bg-white rounded-xl shadow-md border border-gray-100 p-6">
            <div className="text-sm font-semibold text-gray-500 uppercase mb-1">Net Value</div>
            <div className={`text-3xl font-extrabold ${stats.netValue >= 0 ? 'text-green-600' : 'text-red-600'}`}>
              {formatCurrency(stats.netValue)}
            </div>
          </div>
        </div>

        {/* Transaction History Table */}
        <div className="bg-white rounded-xl shadow-md border border-gray-100">
          <div className="px-6 py-4 border-b border-gray-200">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-bold text-gray-900">Transaction Records</h2>
              <div className="flex items-center gap-3">
                <button
                  onClick={fetchTransactions}
                  className="p-2 text-gray-600 hover:text-indigo-600 hover:bg-gray-100 rounded-lg transition"
                  title="Refresh"
                >
                  <RefreshCw className="h-5 w-5" />
                </button>
                <button
                  onClick={exportToCSV}
                  className="p-2 text-gray-600 hover:text-indigo-600 hover:bg-gray-100 rounded-lg transition"
                  title="Export to CSV"
                >
                  <Download className="h-5 w-5" />
                </button>
              </div>
            </div>
          </div>

          {/* Filters */}
          <div className="px-6 py-4 bg-gray-50 border-b border-gray-200 flex flex-wrap items-center gap-4">
            <div className="flex items-center gap-2">
              <Filter className="h-5 w-5 text-gray-600" />
              <span className="text-sm font-medium text-gray-600">Filter:</span>
            </div>
            <div className="flex gap-2">
              {(['ALL', 'BUY', 'SELL'] as const).map(type => (
                <button
                  key={type}
                  onClick={() => setFilterType(type)}
                  className={`px-4 py-1.5 rounded-lg text-sm font-semibold transition ${
                    filterType === type
                      ? 'bg-indigo-600 text-white shadow-md'
                      : 'bg-white text-gray-700 hover:bg-gray-100 border border-gray-200'
                  }`}
                >
                  {type}
                </button>
              ))}
            </div>

            <div className="ml-auto">
              <select
                value={sortOrder}
                onChange={(e) => setSortOrder(e.target.value as 'DESC' | 'ASC')}
                className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              >
                <option value="DESC">Newest First</option>
                <option value="ASC">Oldest First</option>
              </select>
            </div>
          </div>

          {/* Table */}
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                    ID
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                    Symbol
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                    Type
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-semibold text-gray-500 uppercase tracking-wider">
                    Quantity
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-semibold text-gray-500 uppercase tracking-wider">
                    Price
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-semibold text-gray-500 uppercase tracking-wider">
                    Total Value
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                    Date & Time
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredTransactions.length == 0 ? (
                  <tr>
                    <td colSpan={7} className="px-6 py-8 text-center text-gray-500">
                      No transactions found
                    </td>
                  </tr>
                ) : (
                  // 💡 CHANGE HERE: Add 'index' to the map function
                  filteredTransactions.map((transaction, index) => (
                    <tr key={transaction.id} className="hover:bg-gray-50 transition">

                      {/* 💡 CHANGE HERE: Display the sequential number (index + 1) */}
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        #{index + 1}
                      </td>

                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="text-sm font-bold text-gray-900">
                          {transaction.symbol}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold ${
                          transaction.transactionType === 'BUY'
                            ? 'bg-green-100 text-green-800'
                            : 'bg-red-100 text-red-800'
                        }`}>
                          {transaction.transactionType === 'BUY' ? (
                            <ArrowDownRight className="h-3 w-3" />
                          ) : (
                            <ArrowUpRight className="h-3 w-3" />
                          )}
                          {transaction.transactionType}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-right font-medium text-gray-900">
                        {transaction.quantity}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-gray-900">
                        {formatCurrency(transaction.price)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-right font-bold text-gray-900">
                        {formatCurrency(transaction.price * transaction.quantity)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                        {formatDate(transaction.timestamp)}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {/* Footer */}
          <div className="px-6 py-4 bg-gray-50 border-t border-gray-200">
            <div className="text-sm text-gray-600 font-medium">
              Showing {filteredTransactions.length} of {transactions.length} transactions
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TransactionHistory;