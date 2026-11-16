export async function fetchMentionCandidates(query?: string) {
  const params = new URLSearchParams();
  
  if (query) {
    params.append('query', query);
  }
  // ※ channelId はあえて送らず、サーバーから全ユーザーを返してもらう

  const queryString = params.toString() ? `?${params.toString()}` : '';
  
  const response = await fetch(`http://localhost:8080/mention_candidates${queryString}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include', 
  });

  if (!response.ok) {
    throw new Error('メンション候補の取得に失敗しました');
  }

  return response.json();
}