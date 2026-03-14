import { useState, useEffect } from 'react';
import { Sidebar } from '../../components/layout/Sidebar';
import { Button } from '../../components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/table';
import { Badge } from '../../components/ui/badge';
import { Input } from '../../components/ui/input';
import { Edit, Trash2, Plus, Search } from 'lucide-react';
import { Internship } from '../../types';
import { fetchAdminInternships, createAdminInternship } from '../../../services/admin';

import api from '../../../services/api';

export default function ManageInternships() {
  const [searchQuery, setSearchQuery] = useState('');
  const [internships, setInternships] = useState<Internship[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [isAdding, setIsAdding] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  const [formData, setFormData] = useState({
    title: '',
    company: '',
    location: '',
    description: '',
    duration: '',
    stipend: '',
    deadline: '',
    externalLink: '',
    skills: ''
  });

  const filteredInternships = internships.filter((internship) =>
    internship.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
    (internship.company || '')
      .toLowerCase()
      .includes(searchQuery.toLowerCase())
  );

  const handleDelete = (id: string | number) => {
    if (confirm('Are you sure you want to delete this internship?')) {
      setInternships(internships.filter(i => String(i.id) !== String(id)));
      // TODO: call backend delete API when available
    }
  };

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError('');
    
    try {
      const response = await api.post('/admin/internships', formData);
      const newInternship = response.data;
      
      // Immediately refresh the list with the fresh database entity
      setInternships([newInternship, ...internships]);
      
      // Reset form
      setIsAdding(false);
      setFormData({
        title: '', company: '', location: '', description: '',
        duration: '', stipend: '', deadline: '', externalLink: '', skills: ''
      });
    } catch (err: any) {
      console.error(err);
      const backendError = err?.response?.data;
      if (backendError && backendError.message) {
        setError(backendError.message);
      } else if (backendError && backendError.error) {
        setError(backendError.error);
      } else {
        setError(err.message || 'Failed to create internship');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const data = await fetchAdminInternships();
        setInternships(data);
      } catch (err: any) {
        console.error(err);
        setError(err.response?.data?.error || err.message || 'Unable to load internships');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  return (
    <div className="flex min-h-screen bg-gray-50">
      <Sidebar userRole="admin" />
      
      <div className="flex-1">
        <div className="p-8">
          <div className="flex justify-between items-center mb-8">
            <div>
              <h1 className="text-3xl font-semibold text-gray-900 mb-2">
                Manage Internships
              </h1>
              <p className="text-gray-600">
                Add, edit, or remove internship listings
              </p>
            </div>
            <Button onClick={() => setIsAdding(!isAdding)}>
              <Plus className="h-5 w-5 mr-2" />
              {isAdding ? 'Cancel' : 'Add Internship'}
            </Button>
          </div>

          {error && <div className="mb-4 p-4 bg-red-50 text-red-600 rounded-lg">{error}</div>}

          {/* Add Internship Form */}
          {isAdding && (
            <div className="bg-white rounded-lg border border-gray-200 p-6 mb-8 shadow-sm">
              <h2 className="text-xl font-semibold mb-4">Create New Internship</h2>
              <form onSubmit={handleCreateSubmit} className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium mb-1">Title *</label>
                    <Input 
                      required 
                      value={formData.title} 
                      onChange={e => setFormData({...formData, title: e.target.value})} 
                      placeholder="e.g. Software Engineer Intern"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-1">Company / Organization *</label>
                    <Input 
                      required 
                      value={formData.company} 
                      onChange={e => setFormData({...formData, company: e.target.value})} 
                      placeholder="e.g. Google"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-1">Location</label>
                    <Input 
                      value={formData.location} 
                      onChange={e => setFormData({...formData, location: e.target.value})} 
                      placeholder="e.g. Remote, New York"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-1">Duration</label>
                    <Input 
                      value={formData.duration} 
                      onChange={e => setFormData({...formData, duration: e.target.value})} 
                      placeholder="e.g. 3 Months"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-1">Stipend</label>
                    <Input 
                      value={formData.stipend} 
                      onChange={e => setFormData({...formData, stipend: e.target.value})} 
                      placeholder="e.g. $5k/month"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-1">Deadline</label>
                    <Input 
                      type="date"
                      value={formData.deadline} 
                      onChange={e => setFormData({...formData, deadline: e.target.value})} 
                      placeholder="e.g. 2024-12-31"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-1">External Link</label>
                    <Input 
                      value={formData.externalLink} 
                      onChange={e => setFormData({...formData, externalLink: e.target.value})} 
                      placeholder="e.g. https://careers.example.com"
                    />
                  </div>
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium mb-1">Skills (comma separated)</label>
                    <Input 
                      value={formData.skills} 
                      onChange={e => setFormData({...formData, skills: e.target.value})} 
                      placeholder="e.g. Java, Spring Boot, React"
                    />
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">Description</label>
                  <textarea 
                    className="w-full flex min-h-[80px] rounded-md border border-input bg-transparent px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                    rows={4}
                    value={formData.description}
                    onChange={e => setFormData({...formData, description: e.target.value})}
                    placeholder="Describe the responsibilities and requirements..."
                  />
                </div>
                <div className="flex justify-end gap-3 pt-2">
                  <Button variant="outline" type="button" onClick={() => setIsAdding(false)}>
                    Cancel
                  </Button>
                  <Button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? 'Saving...' : 'Save Internship'}
                  </Button>
                </div>
              </form>
            </div>
          )}

          {/* Search */}
          {loading && <p>Loading...</p>}
          <div className="mb-6">
            <div className="relative max-w-md">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
              <Input
                type="text"
                placeholder="Search internships..."
                className="pl-10"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>

          {/* Table */}
          <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Title</TableHead>
                  <TableHead>Company</TableHead>
                  <TableHead>Location</TableHead>
                  <TableHead>Skills</TableHead>
                  <TableHead>Stipend</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredInternships.map((internship) => (
                  <TableRow key={internship.id}>
                    <TableCell className="font-medium">{internship.title}</TableCell>
                    <TableCell>{internship.company}</TableCell>
                    <TableCell>{internship.location || 'Remote'}</TableCell>
                    <TableCell>
                      <div className="flex flex-wrap gap-1">
                        {(internship.skills || []).slice(0, 2).map((skill, index) => (
                          <Badge key={index} variant="secondary" className="text-xs">
                            {typeof skill === 'string' ? skill : (skill as any).name}
                          </Badge>
                        ))}
                        {(internship.skills || []).length > 2 && (
                          <Badge variant="secondary" className="text-xs">
                            +{(internship.skills || []).length - 2}
                          </Badge>
                        )}
                      </div>
                    </TableCell>
                    <TableCell>{internship.stipend || 'Unpaid'}</TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button variant="ghost" size="icon">
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleDelete(internship.id)}
                        >
                          <Trash2 className="h-4 w-4 text-destructive" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
          
          {filteredInternships.length === 0 && (
            <div className="text-center py-12 bg-white rounded-lg border border-gray-200 mt-6">
              <p className="text-gray-500">No internships found.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
