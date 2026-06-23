/**
 * Markdown 渲染器 — 把 AI 回复文本转成美化 HTML
 * 支持：标题(h1-h4)、加粗、斜体、代码块、行内代码、列表（含多行续行）、
 *       表格、引用、分隔线、链接、换行
 */

/** 处理行内格式：加粗、斜体、行内代码、链接 */
export function processInline(text) {
  text = text.replace(/`([^`]+)`/g, '<code class="md-inline-code">$1</code>')
  text = text.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  text = text.replace(/__(.+?)__/g, '<strong>$1</strong>')
  text = text.replace(/~~(.+?)~~/g, '<del>$1</del>')
  text = text.replace(/\*(.+?)\*/g, '<em>$1</em>')
  text = text.replace(/_(.+?)_/g, '<em>$1</em>')
  text = text.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>')
  return text
}

/** 去除行内 Markdown 标记，用于纯文本标题显示 */
export function stripInlineMarkdown(text) {
  return text
    .replace(/\*\*(.+?)\*\*/g, '$1')
    .replace(/__(.+?)__/g, '$1')
    .replace(/~~(.+?)~~/g, '$1')
    .replace(/\*(.+?)\*/g, '$1')
    .replace(/_(.+?)_/g, '$1')
    .replace(/`(.+?)`/g, '$1')
    .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
}

export function renderMarkdown(text) {
  if (!text) return ''
  let html = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  // 1. 代码块 (```) — 先保护起来
  const codeBlocks = []
  html = html.replace(/```(\w*)\n?([\s\S]*?)```/g, (_, lang, code) => {
    const langLabel = lang ? `<div class="md-code-lang">${escapeHtml(lang)}</div>` : ''
    const lines = code.split('\n').map(l => escapeHtml(l)).join('<br>')
    codeBlocks.push(`<div class="md-code-block">${langLabel}<code>${lines}</code></div>`)
    return `\x00CODE${codeBlocks.length - 1}\x00`
  })

  // 2. 行内代码
  html = html.replace(/`([^`]+)`/g, '<code class="md-inline-code">$1</code>')

  // 3. 加粗 + 斜体
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/__(.+?)__/g, '<strong>$1</strong>')
  html = html.replace(/~~(.+?)~~/g, '<del>$1</del>')
  html = html.replace(/\*(.+?)\*/g, '<em>$1</em>')
  html = html.replace(/_(.+?)_/g, '<em>$1</em>')

  // 4. 逐行处理：标题、列表、表格、引用
  const lines = html.split('\n')
  const result = []
  let i = 0
  while (i < lines.length) {
    const line = lines[i]

    // 标题 (h1-h4)
    const headingMatch = line.match(/^(#{1,4})\s+(.+)$/)
    if (headingMatch) {
      const level = headingMatch[1].length
      result.push(`<h${level}>${headingMatch[2]}</h${level}>`)
      i++
      continue
    }

    // 分隔线
    if (/^(-{3,}|\*{3,}|_{3,})$/.test(line.trim())) {
      result.push('<hr>')
      i++
      continue
    }

    // 引用块
    if (/^&gt;\s?/.test(line)) {
      const quoteLines = []
      while (i < lines.length && /^&gt;\s?/.test(lines[i])) {
        quoteLines.push(lines[i].replace(/^&gt;\s?/, ''))
        i++
      }
      result.push(`<blockquote><p>${quoteLines.join('<br>')}</p></blockquote>`)
      continue
    }

    // 表格
    if (line.includes('|') && i + 1 < lines.length && /^\|?[\s-:|]+\|?$/.test(lines[i + 1])) {
      const tableLines = []
      while (i < lines.length && lines[i].includes('|')) {
        tableLines.push(lines[i])
        i++
      }
      if (tableLines.length >= 2) {
        const parseRow = (row) =>
          row.split('|').map(c => c.trim()).filter((c, idx, arr) => !(idx === 0 && c === '') && !(idx === arr.length - 1 && c === ''))

        const headers = parseRow(tableLines[0])
        // 第二行如果是分隔行则跳过，否则当数据行
        const sepLine = tableLines[1]
        const isSep = /^[\s|:-]+$/.test(sepLine.replace(/-/g, '').replace(/\|/g, ''))
        const bodyStart = isSep ? 2 : 1
        const bodyRows = tableLines.slice(bodyStart).map(parseRow)

        let table = '<table><thead><tr>'
        headers.forEach(h => { table += `<th>${h}</th>` })
        table += '</tr></thead><tbody>'
        bodyRows.forEach(row => {
          table += '<tr>'
          row.forEach(cell => { table += `<td>${cell}</td>` })
          table += '</tr>'
        })
        table += '</tbody></table>'
        result.push(table)
        continue
      }
    }

    // 无序列表（支持 -、*、+ 标记，含多行续行）
    if (/^[-*+]\s/.test(line)) {
      const items = []
      while (i < lines.length && /^[-*+]\s/.test(lines[i])) {
        let content = lines[i].replace(/^[-*+]\s/, '')
        i++
        // 续行：非空、不匹配任何块级元素、以 2+ 空格缩进
        while (
          i < lines.length &&
          lines[i].trim() !== '' &&
          !/^#{1,4}\s/.test(lines[i]) &&
          !/^[-*+]\s/.test(lines[i]) &&
          !/^\d+[.、)]\s/.test(lines[i]) &&
          !/^&gt;\s?/.test(lines[i]) &&
          !/^(-{3,}|\*{3,}|_{3,})$/.test(lines[i].trim()) &&
          /^\s{2,}/.test(lines[i])
        ) {
          content += ' ' + lines[i].trim()
          i++
        }
        items.push(`<li>${processInline(content)}</li>`)
      }
      result.push(`<ul>${items.join('')}</ul>`)
      continue
    }

    // 有序列表（支持 .、)、顿号 分隔，含多行续行）
    if (/^\d+[.、)]\s/.test(line)) {
      const items = []
      while (i < lines.length && /^\d+[.、)]\s/.test(lines[i])) {
        let content = lines[i].replace(/^\d+[.、)]\s/, '')
        i++
        while (
          i < lines.length &&
          lines[i].trim() !== '' &&
          !/^#{1,4}\s/.test(lines[i]) &&
          !/^[-*+]\s/.test(lines[i]) &&
          !/^\d+[.、)]\s/.test(lines[i]) &&
          !/^&gt;\s?/.test(lines[i]) &&
          !/^(-{3,}|\*{3,}|_{3,})$/.test(lines[i].trim()) &&
          /^\s{2,}/.test(lines[i])
        ) {
          content += ' ' + lines[i].trim()
          i++
        }
        items.push(`<li>${processInline(content)}</li>`)
      }
      result.push(`<ol>${items.join('')}</ol>`)
      continue
    }

    result.push(line)
    i++
  }
  html = result.join('\n')

  // 5. 段落和换行
  const parts = html.split('\n\n')
  html = parts.map(part => {
    part = part.trim()
    if (!part) return ''
    if (/^<(h[1-6]|ul|ol|table|blockquote|hr|div)/.test(part)) return part
    if (part.includes('\x00CODE')) return part
    return '<p>' + part.replace(/\n/g, '<br>') + '</p>'
  }).join('\n')

  // 6. 恢复代码块
  html = html.replace(/\x00CODE(\d+)\x00/g, (_, i) => codeBlocks[parseInt(i)] || '')

  return html
}

function escapeHtml(text) {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}
