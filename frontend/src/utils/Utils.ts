export function getUserColor(identifier: number | string): string {
  // 白文字（#ffffff）に対してしっかりとコントラストが取れる、見やすいカラーパレット
  const colors = [
    "#E57373", // やさしい赤
    "#81C784", // 爽やかな緑
    "#64B5F6", // 鮮やかな青
    "#FFB74D", // 温かみのあるオレンジ
    "#BA68C8", // 落ち着いた紫
    "#4DB6AC", // ターコイズブルー
    "#F06292", // ピンク
    "#7986CB", // インディゴブルー
    "#A1887F", // ブラウン
    "#90A4AE"  // ブルーグレー
  ];

  let hash = 0;
  const str = String(identifier);
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }

  const index = Math.abs(hash) % colors.length;
  return colors[index];
}