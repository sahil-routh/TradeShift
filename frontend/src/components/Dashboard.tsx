import axios, { type AxiosInstance } from "axios";
import React, { useState, useEffect, useCallback, useRef } from 'react';
// Remove import: TransactionHistory is now a separate page component, not rendered here.
import {
  DollarSign, Clock, LayoutGrid, Zap, TrendingUp, TrendingDown,
  RefreshCw, Briefcase, Minus, Plus, Loader, Search, Star,
  X, ArrowUpRight, ArrowDownRight, BarChart3, ListPlus
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import useAxios from '../hooks/useAxios';
// 🌟 NEW IMPORT: Import useNavigate for routing
import { useNavigate } from 'react-router-dom';

// ========== TYPE DEFINITIONS ==========
interface Asset {
  symbol: string;
  name: string;
  quantity: number;
  avgBuyPrice: number;
  currentPrice: number;
  totalValue: number;
  gainLoss: number;
}

interface PortfolioSummary {
  cashBalance: number;
  totalPortfolioValue: number;
  netWorth: number;
  assets: Asset[];
}

interface TradeResponse {
  success: boolean;
  message: string;
}

interface Stock {
  symbol: string;
  name?: string;
  price: number;
  change: number;
  changePercent: number;
}

interface WatchlistItem {
  id: number;
  symbol: string;
  name: string;
  addedAt: string;
  price: number;
  changePercent: number;
}

interface SearchResult {
  symbol: string;
  name: string;
}

// ========== HELPER FUNCTIONS ==========
const formatCurrency = (value: number) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);

const formatPercentage = (value: number) =>
  `${value > 0 ? '+' : ''}${value.toFixed(2)}%`;

/**
 * Custom Debounce hook/utility to limit the rate of API calls
 * when the user is typing in the search box.
 */
const useDebounce = (callback: Function, delay: number) => {
    const timeoutRef = useRef<number | undefined>();

    useEffect(() => {
        // Cleanup function to clear the timeout when the component unmounts
        return () => {
            if (timeoutRef.current !== undefined) {
                clearTimeout(timeoutRef.current);
            }
        };
    }, []);

    const debouncedCallback = useCallback((...args: any[]) => {
        if (timeoutRef.current !== undefined) {
            clearTimeout(timeoutRef.current);
        }

        timeoutRef.current = window.setTimeout(() => {
            callback(...args);
        }, delay);
    }, [callback, delay]);

    return debouncedCallback;
};


// ========== SUB-COMPONENTS ==========

/** Metric Card Component */
const MetricCard: React.FC<{
  title: string,
  value: number,
  description: string,
  icon: React.ElementType
}> = ({ title, value, description, icon: Icon }) => {
  return (
    <div className="bg-white p-6 rounded-xl shadow-md border border-gray-100 flex flex-col justify-between h-full">
      <div className="flex justify-between items-start mb-2">
        <h3 className="text-sm font-semibold text-gray-500 uppercase">{title}</h3>
        <Icon className="w-5 h-5 text-indigo-500" />
      </div>
      <p className="text-3xl font-extrabold text-gray-900 truncate">{formatCurrency(value)}</p>
      <p className="text-xs text-gray-400 mt-2">{description}</p>
    </div>
  );
};

/** Hot Stocks Panel */
const HotStocksPanel: React.FC<{
  hotStocks: Stock[];
  onRefresh: () => void;
  onSelectStock: (symbol: string, name: string) => void;
  onAddToWatchlist: (symbol: string, name: string) => void;
}> = ({ hotStocks, onRefresh, onSelectStock, onAddToWatchlist }) => {
  return (
    <div className="bg-white rounded-xl shadow-md border border-gray-100 p-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="font-bold text-gray-900 flex items-center text-xl">
          <TrendingUp className="w-5 h-5 mr-2 text-indigo-500" />
          Hot Stocks
        </h3>
        <button
          onClick={onRefresh}
          className="p-2 hover:bg-gray-100 rounded-full transition"
          title="Refresh"
        >
          <RefreshCw className="w-4 h-4 text-gray-400 hover:text-indigo-600" />
        </button>
      </div>

      {/* Table Header: 5 columns: Symbol, Name, Price, G/L %, Action */}
      <div className="hidden md:grid grid-cols-5 text-xs font-semibold uppercase text-gray-500 py-2 border-b">
        <div className="col-span-1">Symbol</div>
        <div className="text-right">Price</div>
        <div className="text-right">G/L %</div>
        <div className="text-right">Action</div>
      </div>

      <div className="space-y-2 max-h-[500px] overflow-y-auto">
        {hotStocks.length === 0 ? (
          <p className="text-sm text-gray-500 text-center py-4">Loading stocks...</p>
        ) : (
          hotStocks.map((stock) => {
            const isUp = stock.changePercent >= 0;
            const changeColor = isUp ? 'text-green-600' : 'text-red-600';
            const TrendIcon = isUp ? ArrowUpRight : ArrowDownRight;

            return (
              <div
                key={stock.symbol}
                className="w-full p-3 rounded-lg hover:bg-gray-50 transition border border-gray-100 grid grid-cols-5 items-center"
              >

                {/* 1. Symbol (Clickable to select for trade) */}
                <button
                    onClick={() => onSelectStock(stock.symbol, stock.name || '')}
                    className="col-span-1 text-left font-bold text-gray-900 hover:text-indigo-600"
                >
                    {stock.symbol}
                </button>


                {/* 3. Price */}
                <div className="text-right font-medium text-gray-800">
                  {formatCurrency(stock.price)}
                </div>

                {/* 4. G/L % */}
                <div className={`text-right font-semibold text-sm flex items-center justify-end ${changeColor}`}>
                  <TrendIcon className="w-3 h-3 mr-1" />
                  {formatPercentage(stock.changePercent)}
                </div>

                {/* 5. Add to Watchlist Button */}
                <div className="text-right">
                    <button
                        onClick={(e) => {
                            e.stopPropagation(); // Prevents stock selection when clicking this button
                            onAddToWatchlist(stock.symbol, stock.name || '');
                        }}
                        className="p-1 text-gray-400 hover:text-yellow-500 rounded-full transition"
                        title="Add to Watchlist"
                    >
                        <ListPlus className="w-5 h-5" />
                    </button>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};

/** Watchlist Component */
const Watchlist: React.FC<{
  watchlist: WatchlistItem[];
  onSelectStock: (symbol: string, name: string) => void;
  onRemove: (symbol: string) => void;
}> = ({ watchlist, onSelectStock, onRemove }) => {
  return (
    <div className="bg-white rounded-xl shadow-md border border-gray-100 p-6">
      <h3 className="font-bold text-gray-900 mb-4 flex items-center text-xl">
        <Star className="w-5 h-5 mr-2 text-yellow-500" />
        My Watchlist
      </h3>

      {watchlist.length === 0 ? (
        <p className="text-sm text-gray-500 text-center py-4">No stocks in watchlist</p>
      ) : (
        <>
            {/* Table Header: 5 columns: Symbol/Name, Price, G/L %, Action */}
            <div className="hidden sm:grid grid-cols-5 text-xs font-semibold uppercase text-gray-500 py-2 border-b">
                <div className="col-span-2">Symbol/Name</div>
                <div className="text-right">Price</div>
                <div className="text-right">G/L %</div>
                <div className="text-right">Action</div>
            </div>

            <div className="space-y-2 max-h-[500px] overflow-y-auto">
            {watchlist.map((item) => {
                const isUp = item.changePercent >= 0;
                const changeColor = isUp ? 'text-green-600' : 'text-red-600';
                const TrendIcon = isUp ? ArrowUpRight : ArrowDownRight;

                return (
                    <div
                        key={item.id}
                        className="grid grid-cols-5 items-center p-3 rounded-lg hover:bg-gray-50 transition border border-gray-100"
                    >
                        {/* 1. Symbol and Name (Col Span 2) */}
                        <button
                            onClick={() => onSelectStock(item.symbol, item.name)}
                            className="col-span-2 text-left py-1 hover:text-indigo-600 transition"
                        >
                            <p className="font-medium text-gray-900">{item.symbol}</p>
                            <p className="text-xs text-gray-500 truncate">{item.name || 'N/A'}</p>
                        </button>

                        {/* 2. Price */}
                        <div className="text-right font-medium text-gray-800">
                            {formatCurrency(item.price)}
                        </div>

                        {/* 3. G/L % */}
                        <div className={`text-right font-semibold text-sm flex items-center justify-end ${changeColor}`}>
                            <TrendIcon className="w-3 h-3 mr-1" />
                            {formatPercentage(item.changePercent)}
                        </div>

                        {/* 4. Remove button */}
                        <div className="text-right">
                            <button
                                onClick={() => onRemove(item.symbol)}
                                className="text-gray-400 hover:text-red-600 p-1"
                                title="Remove from watchlist"
                            >
                                <X className="w-4 h-4" />
                            </button>
                        </div>
                    </div>
                );
            })}
            </div>
        </>
      )}
    </div>
  );
};

/** Enhanced Trade Panel with Search */
const EnhancedTradePanel: React.FC<{
  onTradeExecuted: () => void;
  selectedStock: Stock | null;
  onStockSelect: (symbol: string, name: string) => void;
}> = ({ onTradeExecuted, selectedStock, onStockSelect }) => {
  const api = useAxios();

  const [symbol, setSymbol] = useState('');
  const [quantity, setQuantity] = useState<number | ''>('');
  const [isBuying, setIsBuying] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [response, setResponse] = useState<TradeResponse | null>(null);

  // Search functionality
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<SearchResult[]>([]);
  const [showSearchResults, setShowSearchResults] = useState(false);

  // Update symbol when stock is selected from elsewhere
  useEffect(() => {
    if (selectedStock) {
      setSymbol(selectedStock.symbol);
      setSearchQuery('');
      setShowSearchResults(false);
      setResponse(null); // Clear message when a new stock is selected
    }
  }, [selectedStock]);

  // Search API call logic
  const executeSearch = useCallback(async (query: string) => {
    if (query.length < 1) {
      setSearchResults([]);
      setShowSearchResults(false);
      return;
    }

    try {
      // NOTE: This assumes your API has a search endpoint
      // Adjust the URL if your backend endpoint is different
      const response = await api.get(`/api/finnhub/search?query=${query}`);
      setSearchResults(response.data);
      setShowSearchResults(true);
    } catch (err) {
      console.error('Search error:', err);
      // Optionally show a search error message here
      setSearchResults([{ symbol: 'Error', name: 'Could not fetch results' }]);
      setShowSearchResults(true);
    }
  }, [api]);

  // Debounced search handler (to prevent 429 errors from keypresses)
  const debouncedSearch = useDebounce(executeSearch, 500); // Debounce by 500ms

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const query = e.target.value;
    setSearchQuery(query);
    debouncedSearch(query);
  };

  const selectStockFromSearch = (result: SearchResult) => {
    setSymbol(result.symbol);
    setSearchQuery('');
    setShowSearchResults(false);
    // Also fetch the full quote for the selected stock
    onStockSelect(result.symbol, result.name);
  };

  const handleQuantityChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    if (val === '' || /^\d+$/.test(val)) {
      setQuantity(val === '' ? '' : parseInt(val, 10));
    }
  };

  const executeTrade = async (e: React.FormEvent) => {
    e.preventDefault();

    // FIX: Clear any previous trade response message before a new attempt
    setResponse(null);

    if (!symbol || !quantity || quantity <= 0) {
      setResponse({ success: false, message: "Please enter a valid stock symbol and quantity." });
      return;
    }

    setIsLoading(true);

    const tradeType = isBuying ? 'BUY' : 'SELL';
    const endpoint = `/api/portfolio/trade/${tradeType.toLowerCase()}`;

    try {
      const payload = {
        symbol: symbol.toUpperCase(),
        quantity: quantity,
      };

      const res = await api.post(endpoint, payload);
      setResponse({
        success: true,
        message: res.data.message || `${tradeType} trade executed successfully!`
      });
      setSymbol('');
      setQuantity('');
      onTradeExecuted();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || `Failed to execute ${tradeType} trade.`;
      setResponse({ success: false, message: msg });
    } finally {
      setIsLoading(false);
    }
  };

  const buttonColor = isBuying ? 'bg-green-600 hover:bg-green-700' : 'bg-red-600 hover:bg-red-700';

  return (
    <div className="bg-white p-6 rounded-xl shadow-md border border-gray-100 h-full">
      <h2 className="text-xl font-bold text-gray-800 mb-4 flex items-center">
        <Zap className="w-5 h-5 mr-2 text-indigo-500" />
        Execute Trade
      </h2>

      <form onSubmit={executeTrade} className="space-y-4">
        {/* Stock Search */}
        <div className="relative">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Search Stock
          </label>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
            <input
              type="text"
              value={searchQuery}
              onChange={handleSearchChange} // Use the new debounced handler
              placeholder="Search stocks (e.g., Apple, AAPL)..."
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
            />
          </div>

          {/* Search Results Dropdown */}
          {showSearchResults && searchResults.length > 0 && (
            <div className="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg max-h-48 overflow-y-auto">
              {searchResults.map((result) => (
                <button
                  key={result.symbol}
                  type="button"
                  onClick={() => selectStockFromSearch(result)}
                  className="w-full text-left px-4 py-2 hover:bg-gray-50 border-b last:border-b-0"
                >
                  <p className="font-semibold text-gray-900">{result.symbol}</p>
                  <p className="text-sm text-gray-600">{result.name}</p>
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Selected Stock Info */}
        {selectedStock && (
          <div className="bg-indigo-50 rounded-lg p-3">
            <div className="flex justify-between items-start">
              <div>
                <p className="font-bold text-lg text-gray-900">{selectedStock.symbol}</p>
                <p className="text-sm text-gray-600">{selectedStock.name}</p>
              </div>
              <div className="text-right">
                <p className="text-xl font-bold text-gray-900">
                  {formatCurrency(selectedStock.price)}
                </p>
                <p className={`text-sm font-semibold ${
                  selectedStock.change >= 0 ? 'text-green-600' : 'text-red-600'
                }`}>
                  {formatPercentage(selectedStock.changePercent)}
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Stock Symbol Input */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Stock Symbol
          </label>
          <input
            type="text"
            value={symbol}
            onChange={(e) => setSymbol(e.target.value)}
            className="w-full rounded-md border-gray-300 shadow-sm p-2 border focus:ring-indigo-500 focus:border-indigo-500 uppercase"
            placeholder="e.g., AAPL"
            required
          />
        </div>

        {/* Quantity Input */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Quantity
          </label>
          <input
            type="number"
            min="1"
            value={quantity}
            onChange={handleQuantityChange}
            className="w-full rounded-md border-gray-300 shadow-sm p-2 border focus:ring-indigo-500 focus:border-indigo-500"
            placeholder="Number of shares"
            required
          />
        </div>

        {/* Buy/Sell Toggle */}
        <div className="flex space-x-4">
          <button
            type="button"
            onClick={() => setIsBuying(true)}
            className={`w-1/2 py-2 rounded-lg font-semibold transition ${
              isBuying
                ? 'bg-green-500 text-white shadow-lg'
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            }`}
          >
            Buy
          </button>
          <button
            type="button"
            onClick={() => setIsBuying(false)}
            className={`w-1/2 py-2 rounded-lg font-semibold transition ${
              !isBuying
                ? 'bg-red-500 text-white shadow-lg'
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            }`}
          >
            Sell
          </button>
        </div>

        {/* Submit Button */}
        <button
          type="submit"
          disabled={isLoading}
          className={`w-full py-3 rounded-lg text-white font-bold transition shadow-xl ${buttonColor} ${
            isLoading ? 'opacity-60 cursor-not-allowed' : ''
          }`}
        >
          {isLoading ? (
            <Loader className="w-5 h-5 animate-spin mx-auto" />
          ) : (
            `Confirm ${isBuying ? 'BUY' : 'SELL'}`
          )}
        </button>
      </form>

      {/* Response Message */}
      {response && (
        <div className={`mt-4 p-3 rounded-lg text-sm font-medium ${
          response.success
            ? 'bg-green-100 text-green-800'
            : 'bg-red-100 text-red-800'
        }`}>
          {response.message}
        </div>
      )}
    </div>
  );
};

/** Current Holdings Component */
const CurrentHoldings: React.FC<{ assets: Asset[] }> = ({ assets }) => {
  return (
    <div className="bg-white p-6 rounded-xl shadow-md border border-gray-100 h-full">
      <h2 className="text-xl font-bold text-gray-800 mb-4 flex items-center">
        <Briefcase className="w-5 h-5 mr-2 text-indigo-500" />
        Current Holdings ({assets.length})
      </h2>

      {assets.length === 0 ? (
        <div className="text-center py-10 text-gray-500">
          <LayoutGrid className="w-12 h-12 mx-auto mb-3" />
          <p className="text-lg">Your portfolio is currently empty.</p>
          <p className="text-sm mt-1">Use the trade panel to make your first investment!</p>
        </div>
      ) : (
        <div className="space-y-3 overflow-y-auto max-h-[400px]">
          {/* Table Header */}
          <div className="hidden sm:grid grid-cols-5 text-xs font-semibold uppercase text-gray-500 pb-2 border-b">
            <div className="col-span-2">Stock</div>
            <div className="text-right">Quantity</div>
            <div className="text-right">Price</div>
            <div className="text-right">G/L %</div>
          </div>

          {/* Holdings List */}
          {assets.map((asset) => {
            const isUp = asset.gainLoss >= 0;
            const changeColor = isUp ? 'text-green-600' : 'text-red-600';
            const TrendIcon = isUp ? TrendingUp : TrendingDown;

            return (
              <div
                key={asset.symbol}
                className="grid grid-cols-5 items-center py-3 border-b last:border-b-0 hover:bg-gray-50 rounded-lg transition"
              >
                <div className="col-span-2 flex flex-col">
                  <span className="font-bold text-gray-900">{asset.symbol}</span>
                  <span className="text-xs text-gray-500 truncate">{asset.name}</span>
                </div>

                <div className="text-right font-medium text-gray-800">
                  {asset.quantity}
                </div>

                <div className="text-right text-sm text-gray-600">
                  {formatCurrency(asset.currentPrice)}
                </div>

                <div className={`text-right font-semibold text-sm flex items-center justify-end ${changeColor}`}>
                  <TrendIcon className="w-4 h-4 mr-1" />
                  {formatPercentage(asset.gainLoss)}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};


// ========== MAIN DASHBOARD COMPONENT ==========
const Dashboard: React.FC = () => {
  const { user, isAuthenticated, isLoading: isAuthLoading } = useAuth();
  const api = useAxios();
  // 🌟 NEW: Initialize useNavigate hook
  const navigate = useNavigate();

  // UI State
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentTime, setCurrentTime] = useState(new Date());
  // ❌ REMOVED: isHistoryOpen state is no longer needed

  // Data State
  const [portfolio, setPortfolio] = useState<PortfolioSummary | null>(null);
  const [hotStocks, setHotStocks] = useState<Stock[]>([]);
  const [watchlist, setWatchlist] = useState<WatchlistItem[]>([]);
  const [selectedStock, setSelectedStock] = useState<Stock | null>(null);

  // 🌟 NEW FUNCTION: Handle navigation to the Transaction History page
  const handleViewHistory = () => {
    navigate('/history'); // Navigate to the new route
  };


  // Fetch Portfolio Data
  const fetchData = useCallback(async () => {
    if (!isAuthenticated) {
      setError("Authentication required. Please log in.");
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const portfolioResponse = await api.get('/api/portfolio');
      const data: PortfolioSummary = portfolioResponse.data;

      // 1. Calculate individual asset values and gain/loss
      const assetsWithCalculations = (data.assets || []).map(asset => {
        const totalCost = asset.quantity * asset.avgBuyPrice;
        const totalMarketValue = asset.quantity * asset.currentPrice;
        const gainLossAbsolute = totalMarketValue - totalCost;
        const gainLossPercent = totalCost === 0 ? 0 : (gainLossAbsolute / totalCost) * 100;

        return {
          ...asset,
          gainLoss: gainLossPercent,
          totalValue: totalMarketValue, // Asset's current market value
        };
      });

      // 2. Calculate Total Portfolio Value (Market Value) by summing up all asset market values.
      const totalValue = assetsWithCalculations.reduce((sum, asset) => sum + asset.totalValue, 0);

      const cashBalance = data.cashBalance || 0;

      // 3. Calculate Net Worth
      data.netWorth = cashBalance + totalValue;

      setPortfolio({
        ...data,
        totalPortfolioValue: totalValue,
        assets: assetsWithCalculations
      });
    } catch (err: any) {
      const status = err.response?.status;
      let msg = err.response?.data?.message || err.message || "Failed to fetch portfolio data.";

      if (status === 403) {
        msg = `Access Denied (403). Please try logging in again.`;
      } else if (status === 401) {
        msg = `Authentication Required (401). Please log in.`;
      }

      console.error("Dashboard Error:", msg, err);
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated, api]);

  // Fetch Hot Stocks
  const fetchHotStocks = useCallback(async () => {
    try {
      const response = await api.get('/api/finnhub/major-stocks?symbols=AAPL,MSFT,GOOGL,AMZN,TSLA,META,NVDA,JPM,V,WMT,DIS,NFLX,PYPL,INTC,CSCO,PEP,KO,NKE,BA,ADBE,ORCL,IBM,CRM,AMD,XOM,CVX,PFE,ABT,T,VZ,UBER,LYFT,SHOP,SQ,BABA,SONY,TSM,NIO,DELL,HPQ,C,GS,MS,SPOT,INTU,MCD,SBUX,TMO,LLY,UNH');

      setHotStocks(response.data);
    } catch (err) {
      console.error('Error fetching hot stocks:', err);
    }
  }, [api]);

  // Fetch Watchlist
  const fetchWatchlist = useCallback(async () => {
    try {
      // 1. Fetch the list of watchlist symbols
      const watchlistResponse = await api.get('/api/watchlist');
      const symbols: { symbol: string; name: string; id: number; addedAt: string; }[] = watchlistResponse.data;

      if (symbols.length === 0) {
        setWatchlist([]);
        return;
      }

      // 2. Prepare symbols string for quote API (e.g., AAPL,MSFT,GOOGL)
      const symbolString = symbols.map(s => s.symbol).join(',');

      // 3. Fetch live quote data for all symbols
      const quoteResponse = await api.get(`/api/finnhub/major-stocks?symbols=${symbolString}`);
      const quotes: Stock[] = quoteResponse.data;

      // 4. Merge the watchlist data with live quote data
      const updatedWatchlist: WatchlistItem[] = symbols.map(item => {
        const quote = quotes.find(q => q.symbol === item.symbol);

        return {
          ...item,
          price: quote?.price ?? 0,
          changePercent: quote?.changePercent ?? 0,
        } as WatchlistItem;
      });

      setWatchlist(updatedWatchlist);
    } catch (err) {
      console.error('Error fetching watchlist:', err);
    }
  }, [api]);

  // Select Stock (fetch live quote)
  const selectStock = async (symbol: string, name: string) => {
    try {
      const response = await api.get(`/api/finnhub/quote/${symbol}`);
      const quote = response.data;
      setSelectedStock({
        symbol,
        name,
        price: quote.c,
        change: quote.d,
        changePercent: quote.dp
      });
    } catch (err) {
      console.error('Error fetching stock quote:', err);
    }
  };

  // Add to Watchlist
  const addToWatchlist = async (symbol: string, name: string) => {
    try {
      await api.post('/api/watchlist', { symbol, name });
      fetchWatchlist(); // Refresh the watchlist to show the new stock
    } catch (err) {
      console.error('Error adding to watchlist:', err);
      // Optional: Add a user notification/toast here
    }
  };

  // Remove from Watchlist
  const removeFromWatchlist = async (symbol: string) => {
    try {
      await api.delete(`/api/watchlist/${symbol}`);
      fetchWatchlist();
    } catch (err) {
      console.error('Error removing from watchlist:', err);
    }
  };


  // Initial Load
  useEffect(() => {
    if (!isAuthLoading && isAuthenticated) {
      fetchData();
      fetchHotStocks();
      fetchWatchlist();
    } else if (!isAuthLoading && !isAuthenticated) {
      setIsLoading(false);
    }
  }, [isAuthLoading, isAuthenticated, fetchData, fetchHotStocks, fetchWatchlist]);

  // Auto-refresh Hot Stocks and Watchlist
  useEffect(() => {
    // FIX: Increased interval to 3 minutes (180000ms) to avoid hitting API rate limits (429 errors)
    const refreshInterval = 180000;

    const hotStockInterval = setInterval(fetchHotStocks, refreshInterval);
    const watchlistInterval = setInterval(fetchWatchlist, refreshInterval);

    return () => {
        clearInterval(hotStockInterval);
        clearInterval(watchlistInterval);
    }
  }, [fetchHotStocks, fetchWatchlist]);

  // Clock Update
  useEffect(() => {
    const interval = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(interval);
  }, []);

  // Loading State
  if (isAuthLoading || isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <Loader className="w-12 h-12 animate-spin text-indigo-600 mx-auto mb-4" />
          <p className="text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  // Error State
  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center p-8 bg-white rounded-xl shadow-lg max-w-md">
          <p className="text-red-600 mb-4 font-semibold">{error}</p>
          <button
            onClick={fetchData}
            className="px-6 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  const { netWorth = 0, cashBalance = 0, totalPortfolioValue = 0, assets = [] } = portfolio || {};

  return (
    <div className="min-h-screen bg-gray-50 p-4 sm:p-8 font-['Inter']">
      {/* Header */}
      <header className="mb-8 flex justify-between items-start flex-wrap">
        <div>
          <h1 className="text-3xl font-extrabold text-gray-900">TradeShift Dashboard</h1>
          <p className="text-gray-500 flex items-center mt-1">
            <Clock className="w-4 h-4 mr-2" />
            Live as of: {currentTime.toLocaleTimeString()}
          </p>
        </div>

        {/* VIEW TRANSACTION HISTORY BUTTON & USER INFO */}
        <div className="flex items-center space-x-4 mt-4 sm:mt-0">
          <button
            // 🌟 UPDATED: Use the navigation handler
            onClick={handleViewHistory}
            className="px-4 py-2 bg-indigo-600 text-white rounded-lg font-semibold hover:bg-indigo-700 transition flex items-center shadow-md"
          >
            <BarChart3 className="w-5 h-5 mr-2" />
            View Transaction History
          </button>

          {user && (
            <div className="text-right p-2 rounded-lg bg-white shadow-sm border border-gray-200">
              <p className="font-semibold text-gray-800">{user.name}</p>
              <p className="text-sm text-gray-500">{user.email}</p>
            </div>
          )}
        </div>
      </header>

      {/* Metrics Grid (Row 1: Net Worth, Cash Balance, Market Value) */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <MetricCard
          title="NET WORTH"
          value={netWorth}
          description="Total value including cash and assets"
          icon={DollarSign}
        />
        <MetricCard
          title="CASH BALANCE"
          value={cashBalance}
          description="Available funds for trading"
          icon={Minus}
        />
        <MetricCard
          title="MARKET VALUE"
          value={totalPortfolioValue}
          description="Total current value of all holdings"
          icon={Plus}
        />
      </div>

      {/* Row 2: Hot Stocks (Full Width) */}
      <div className="mb-8">
        <HotStocksPanel
          hotStocks={hotStocks}
          onRefresh={fetchHotStocks}
          onSelectStock={selectStock}
          onAddToWatchlist={addToWatchlist}
        />
      </div>

      {/* Row 3: Main Trading Area (Execute Trade & Current Holdings) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 mb-6">

        {/* Left/Center Column: Execute Trade (Wider Column) */}
        <div className="lg:col-span-7">
          <EnhancedTradePanel
            onTradeExecuted={fetchData}
            selectedStock={selectedStock}
            onStockSelect={selectStock}
          />
        </div>

        {/* Right Column: Current Holdings (Narrower Column) */}
        <div className="lg:col-span-5">
          <CurrentHoldings assets={assets} />
        </div>
      </div>

      {/* Row 4: Watchlist (Full Width) */}
      <div className="mt-8">
        <Watchlist
          watchlist={watchlist}
          onSelectStock={selectStock}
          onRemove={removeFromWatchlist}
        />
      </div>

      {/* ❌ REMOVED: The conditional rendering block for TransactionHistory has been removed.
          The TransactionHistory component should now be rendered by your main router
          at the '/history' path.
      */}

    </div>
  );
};

export default Dashboard;