import React, { useState, useRef } from 'react';
import axios from 'axios';
import { AnalysisResult } from '../types';

interface Props {
    onAnalysisComplete: (result: AnalysisResult) => void;
  }

const ResumeUpload: React.FC<Props> = ({ onAnalysisComplete }) => {
    const [file, setFile] = useState<File | null>(null);
    const [jobDescription, setJobDescription] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [dragOver, setDragOver] = useState(false);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
          const selected = e.target.files?.[0];
          if (selected) {
                  validateAndSetFile(selected);
                }
        };

    const validateAndSetFile = (selected: File) => {
          const allowed = ['application/pdf', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
          if (!allowed.includes(selected.type)) {
                  setError('Only PDF and DOCX files are supported');
                  return;
                }
          if (selected.size > 5 * 1024 * 1024) {
                  setError('File size must be under 5MB');
                  return;
                }
          setError('');
          setFile(selected);
        };

    const handleDrop = (e: React.DragEvent) => {
          e.preventDefault();
          setDragOver(false);
          const dropped = e.dataTransfer.files[0];
          if (dropped) validateAndSetFile(dropped);
        };

    const handleSubmit = async (e: React.FormEvent) => {
          e.preventDefault();
          if (!file || !jobDescription.trim()) {
                  setError('Please upload a resume and paste a job description');
                  return;
                }

          const formData = new FormData();
          formData.append('file', file);
          formData.append('jobDescription', jobDescription);

          setLoading(true);
          setError('');

          try {
                  const token = localStorage.getItem('token');
                  const response = await axios.post('/api/resume/analyze', formData, {
                            headers: {
                                        'Content-Type': 'multipart/form-data',
                                        Authorization: `Bearer ${token}`,
                                      },
                          });
                  onAnalysisComplete(response.data);
                } catch (err: any) {
                  setError(err.response?.data?.message || 'Something went wrong, try again');
                } finally {
                  setLoading(false);
                }
        };

    return (
          <div className="max-w-2xl mx-auto p-6">
            <h2 className="text-2xl font-bold mb-6">Analyze Your Resume</h2>

            <form onSubmit={handleSubmit} className="space-y-6">
              {/* drag drop zone */}
              <div
                className={`border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors ${
                              dragOver ? 'border-blue-500 bg-blue-50' : 'border-gray-300 hover:border-gray-400'
                            }`}
                onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
                onDragLeave={() => setDragOver(false)}
                onDrop={handleDrop}
                onClick={() => fileInputRef.current?.click()}
              >
                <input
                  ref={fileInputRef}
                  type="file"
                  accept=".pdf,.docx"
                  onChange={handleFileChange}
                  className="hidden"
                />
                {file ? (
                              <div>
                                <p className="text-green-600 font-medium">{file.name}</p>
                                <p className="text-sm text-gray-500">{(file.size / 1024).toFixed(1)} KB</p>
                              </div>
                            ) : (
                              <div>
                                <p className="text-gray-600">Drop your resume here or click to browse</p>
                                <p className="text-sm text-gray-400 mt-1">PDF or DOCX, max 5MB</p>
                              </div>
                            )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Job Description
                </label>
                <textarea
                  value={jobDescription}
                  onChange={(e) => setJobDescription(e.target.value)}
                  rows={8}
                  placeholder="Paste the full job description here..."
                  className="w-full border rounded-lg p-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              {error && <p className="text-red-500 text-sm">{error}</p>}

              <button
                type="submit"
                disabled={loading}
                className="w-full bg-blue-600 text-white py-3 px-6 rounded-lg font-medium hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                {loading ? 'Analyzing...' : 'Analyze Resume'}
              </button>
            </form>
          </div>
        );
  };

export default ResumeUpload;
