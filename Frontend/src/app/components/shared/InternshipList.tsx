import { useEffect, useState } from 'react';
import { Loader2, AlertCircle, RefreshCw } from 'lucide-react';
import { Button } from '../ui/button';
import { Alert, AlertDescription } from '../ui/alert';
import { Card } from '../ui/card';
import { Badge } from '../ui/badge';
import api from '../../../services/api';

interface BackendInternship {
  id: number;
  title: string;
  company: string;
  location: string;
  skills: string | string[];
  source: string;
  externalJobId: string;
  createdAt: string;
}

/** Safely normalize skills to a string array regardless of backend shape */
function normalizeSkills(skills: string | string[] | undefined | null): string[] {
  if (!skills) return [];
  if (Array.isArray(skills)) return skills.map(s => String(s).trim()).filter(Boolean);
  return String(skills).split(/,\s*/).map(s => s.trim()).filter(Boolean);
}

interface InternshipListProps {
  onFetchComplete?: (count: number) => void;
}

export function InternshipList({ onFetchComplete }: InternshipListProps) {
  const [internships, setInternships] = useState<BackendInternship[]>([]);
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  /**
   * Fetch internships from backend
   */
  const loadInternships = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.get<{
        success: boolean;
        count: number;
        data: BackendInternship[];
      }>('/internships');

      if (response.data.success) {
        setInternships(response.data.data);
      } else {
        setError('Failed to load internships');
      }
    } catch (err: any) {
      const errorMessage = err.response?.data?.error || 'Failed to connect to backend';
      setError(errorMessage);
      console.error('Error loading internships:', err);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Fetch internships from external API and save to database
   */
  const fetchFromExternalAPI = async () => {
    setFetching(true);
    setError(null);
    setSuccessMessage(null);
    try {
      const response = await api.get<{
        success: boolean;
        newCount: number;
        totalCount: number;
        message: string;
        data: BackendInternship[];
      }>('/internships/fetch', {
        params: {
          query: 'internship software',
          pages: 2,
        },
      });

      if (response.data.success) {
        setInternships(response.data.data);
        setSuccessMessage(
          `Successfully fetched ${response.data.newCount} new internships. Total: ${response.data.totalCount}`
        );
        onFetchComplete?.(response.data.newCount);

        // Auto-dismiss success message after 5 seconds
        setTimeout(() => setSuccessMessage(null), 5000);
      } else {
        setError('Failed to fetch internships from external API');
      }
    } catch (err: any) {
      const errorMessage = err.response?.data?.error || 'Failed to fetch from external API';
      setError(errorMessage);
      console.error('Error fetching from external API:', err);
    } finally {
      setFetching(false);
    }
  };

  // Load internships on component mount
  useEffect(() => {
    loadInternships();
  }, []);

  return (
    <div className="w-full">
      {/* Header Section */}
      <div className="mb-6 flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Internships</h2>
          <p className="text-gray-600">
            {internships.length} internships available
          </p>
        </div>
        <div className="flex gap-2">
          <Button
            onClick={loadInternships}
            disabled={loading}
            variant="outline"
            className="flex items-center gap-2"
          >
            {loading ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <RefreshCw className="h-4 w-4" />
            )}
            Refresh
          </Button>
          <Button
            onClick={fetchFromExternalAPI}
            disabled={fetching}
            className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white"
          >
            {fetching ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                Fetching...
              </>
            ) : (
              <>
                <RefreshCw className="h-4 w-4" />
                Fetch New Internships
              </>
            )}
          </Button>
        </div>
      </div>

      {/* Success Message */}
      {successMessage && (
        <Alert className="mb-6 bg-green-50 border-green-200">
          <AlertDescription className="text-green-800">
            ✓ {successMessage}
          </AlertDescription>
        </Alert>
      )}

      {/* Error Alert */}
      {error && (
        <Alert className="mb-6 bg-red-50 border-red-200">
          <AlertCircle className="h-4 w-4 text-red-600" />
          <AlertDescription className="text-red-700">{error}</AlertDescription>
        </Alert>
      )}

      {/* Loading State */}
      {loading && (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
          <span className="ml-2 text-gray-600">Loading internships...</span>
        </div>
      )}

      {/* Empty State */}
      {!loading && internships.length === 0 && (
        <Card className="p-12 text-center bg-gray-50 border-dashed">
          <p className="text-gray-600 mb-4">No internships found</p>
          <Button
            onClick={fetchFromExternalAPI}
            disabled={fetching}
            className="bg-blue-600 hover:bg-blue-700 text-white"
          >
            {fetching ? 'Fetching...' : 'Fetch internships from API'}
          </Button>
        </Card>
      )}

      {/* Internships Grid */}
      {!loading && internships.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {internships.map((internship) => (
            <Card
              key={internship.id}
              className="p-4 hover:shadow-lg transition-shadow"
            >
              <div className="mb-3">
                <h3 className="text-lg font-semibold text-gray-900 line-clamp-2">
                  {internship.title}
                </h3>
                <p className="text-sm text-gray-600">{internship.company}</p>
              </div>

              <div className="mb-3">
                <p className="text-xs text-gray-500 mb-2">
                  📍 {internship.location}
                </p>
                <p className="text-xs text-gray-500">
                  🔗 Source: <span className="font-medium">{internship.source}</span>
                </p>
              </div>

              {/* Skills */}
              <div className="mb-3">
                <p className="text-xs font-semibold text-gray-700 mb-2">
                  Required Skills:
                </p>
                <div className="flex flex-wrap gap-1">
                  {(() => {
                    const skillList = normalizeSkills(internship.skills);
                    return (
                      <>
                        {skillList.slice(0, 3).map((skill, idx) => (
                          <Badge
                            key={idx}
                            className="text-xs bg-blue-100 text-blue-800 hover:bg-blue-100"
                          >
                            {skill}
                          </Badge>
                        ))}
                        {skillList.length > 3 && (
                          <Badge className="text-xs bg-gray-100 text-gray-800 hover:bg-gray-100">
                            +{skillList.length - 3} more
                          </Badge>
                        )}
                      </>
                    );
                  })()}
                </div>
              </div>

              {/* Meta Info */}
              <div className="text-xs text-gray-500 border-t pt-2">
                <p>
                  Posted:{' '}
                  {new Date(internship.createdAt).toLocaleDateString()}
                </p>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
