/*********************************************************************************************************
 *     Use this scritp to generate the NAACCR documentation from their website. 
 *
 *     !!!!    MAKE SURE TO AUTO-FORMAT THE ENTIRE FOLDER AFTER CREATING THE FILES    !!!!
 *
 *     This script also creates the styles that need to be copied into the AbstractNaaccrLayout class.
 *
 *     After creting the files with this script, use the "NaaccrDocViewer" and review every created file;
 *     that utility class is in the SEER*Utils GUI project (test/naaccr)
 *
 *     2014/10/25 FPD - didn't need to redo the styles for NAACCR 15, they were the same as NAACCR 14...
 *
 *********************************************************************************************************/

// TODO FPD move the NaaccrDocViewer and NaaccrDocChecker to this project!
// TODO FPD this code doesn't handle Windows characters! See history of "recordType.html", or "tumorRecordNumber.html" for bad files...
// TODO FPD looks like the files created by this script are either ASCII, or UTF-8 without a BOM; looks like the HTML renderer doesn't like that; maybe it needs the BOM...


// output directory
def outputDir = new File('C:\\dev\\projects\\layout\\src\\main\\resources\\layout\\fixed\\naaccr\\doc\\naaccr16')

// the layout to use to gather the fields
def layout = new File(outputDir, '..\\..\\naaccr-16-layout.xml')

// this is the URL to read the full HTML page from
def url = 'http://www.naaccr.org/Applications/ContentReader/Default.aspx?c=10'

// this is the URL to read the style sheet from
def styleSheetUrl = 'http://www.naaccr.org/Applications/Styles/ContentReader.css'

// create the stylesheet
StringBuilder styleBuf = new StringBuilder()
def styleSheet = new URL(styleSheetUrl).getText()
for (String line : styleSheet.replace('{\r\n', '{').replace(';\r\n', ';').replace('\r\n\r\n', '\r\n').replace('    ', ' ').split("\\r?\\n")) {
    line = line.trim()
    if (!line.isEmpty() && !line.contains('body') && !line.contains('/*') && !line.contains('*/') && !line.startsWith('.mark-changed') && !line.contains('Times New Roman') && !line.contains('font-size'))
        styleBuf.append('        _DEFAULT_CSS_14_AND_LATER.append("').append(line).append('\\n");\n')
}
// the printed style should be copied into the NaaccrLayout class...
println styleBuf

// read the page
def fullContent = new URL(url).getText() // TODO this should probably specify the UTF-8 encoding somehow...

// split by anchors
def items = [:]
def matcher = fullContent =~ /<a\sname='(\d+)'><\/a>/
def itemNumber = ''
def start = 0
while (matcher.find(start)) {
    if (start != 0)
        items.put(itemNumber, fullContent.substring(start, matcher.start()))
    itemNumber = matcher.group(1)
    start = matcher.end()
}

// gather the fields so can use the property names as file names
def fields = [:]
new XmlSlurper().parse(layout).field.each { f ->
    if (!f.@"naaccr-item-num".text().isEmpty())
        fields.put(f.@"naaccr-item-num".text().toString(), f.@name.text())
    if (f.field != null) {
        f.field.each { ff ->
            if (!ff.@"naaccr-item-num".text().isEmpty())
                fields.put(ff.@"naaccr-item-num".text().toString(), ff.@name.text())
        }
    }
}

// go through each field and create the corresponding file
outputDir.mkdirs()
fields.each() { itemNum, prop ->
    String html = items.get(itemNum)
    if (html != null) {

        // bad & characters
        html = cleanupHtml(html)

        // special cases
        if (itemNum == '145')
            html = html.replace('<5%', '&lt;5%').replace('<10%', '&lt;10%').replace('<20%', '&lt;20%')
        else if (itemNum == '170' || itemNum == '180')
            html = html.replace('SEER < 1988', 'SEER &lt; 1988')
        else if (itemNum == '2130')
            html = html.replace('<i>2011 SEER Coding Manual', '<i>2011 SEER Coding Manual</i>')
        else if (itemNum == '2085')
            html = html.replace('<span style="font-family:Symbol;">&#183;<span style="Times New Roman&quot;">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</span></span>', '')
        else if (itemNum == '2690') {
            html = html.replace('<li>NAACCR-approved', '</li><li>NAACCR-approved').replace('<li>Do not repeat', '</li><li>Do not repeat')
            html = html.replace('<li>Additional comments', '</li><li>Additional comments').replace('<li>If information', '</li><li>If information')
            html = html.replace('<li>Do not include irrelevant', '</li><li>Do not include irrelevant').replace('<li>Do not include information', '</li><li>Do not include information')
            html = html.replace('<ul>For out-of-state', '<br/>For out-of-state')
        }

        // parse out the table
        int idx = html.indexOf('</table>')
        String summary = cleanupSummaryTable((String) prop, html.substring(0, idx + 8))
        String content = html.substring(idx + 8)

        // clean up some table borders
        if (itemNum in ['2330', '2350', '2660', '2640', '2650', '2670', '2620', '2610', '560', '2550', '2560', '2570', '2520', '2540', '2530', '2590', '2580', '2600'])
            content = content.replace('<table style=', '<table class="naaccr-borders" style=').replace('<td style=', '<td class="naaccr-borders" style=')

        // write the resulting file
        def output = new File(outputDir, "${prop}.html")
        output.text = cleanupHtml(summary) + content
    } else
        println "Can't find content for item number ${itemNum}"
}

def cleanupHtml(String html) {

    // make sure we don't replace codes that are already escaped (there are many other codes, I am handling only the most common ones...)
    html = html.replace('&quot;', '@@@QUOTE@@@')
    html = html.replace('&amp;', '@@@AND@@@')
    html = html.replace('&lt;', '@@@LESS@@@')
    html = html.replace('&gt;', '@@@GREATER@@@')
    html = html.replace('&nbsp;', '@@@SPACE@@@')
    html = html.replace('&#', '@@@NUMBER@@@')

    // then replace the remaining & by the escaped version
    html = html.replace('&', '&amp;')

    // and finally, replace back the original escaped ones
    html = html.replace('@@@QUOTE@@@', '&quot;')
    html = html.replace('@@@AND@@@', '&amp;')
    html = html.replace('@@@LESS@@@', '&lt;')
    html = html.replace('@@@GREATER@@@', '&gt;')
    html = html.replace('@@@SPACE@@@', '&nbsp;')
    html = html.replace('@@@NUMBER@@@', '&#')

    return html
}

// it's easier to re-construct the entire table than trying to replace part of it...
def cleanupSummaryTable(String propName, String txt) {

    // we need to preserver the new lines...
    txt = txt.replaceAll('<br>|<br/>|<br />', '@@@NEWLINE@@@')

    try {
        StringBuilder buf = new StringBuilder()
        buf.append("<table class=\"naaccr-summary-table naaccr-borders\">")

        new XmlSlurper().parseText(txt).tr.eachWithIndex { row, rowIdx ->
            // we totally skip rowIdx 0, which is the field name
            if (rowIdx == 1) { // table headers
                buf.append("\n  <tr>")
                row.td.eachWithIndex { col, coldIdx ->
                    buf.append("<th class=\"naaccr-summary-header naaccr-borders\">").append(col).append("</th>")
                }
                buf.append("</tr>")
            } else if (rowIdx == 2) { // table data
                buf.append("\n  <tr>")
                row.td.eachWithIndex { col, colIdx ->
                    if (colIdx == 0)
                        buf.append("<td class=\"naaccr-summary-cell naaccr-borders\">").append(col).append("</td>")
                    else
                        buf.append("<td class=\"naaccr-summary-cell naaccr-borders naaccr-summary-centered\">").append(col).append("</td>")
                }
                buf.append("</tr>")
            }
        }
        buf.append("\n</table>\n\n")

        return buf.toString().replaceAll('@@@NEWLINE@@@', '<br/>')
    }
    catch (Exception e) {
        println "!!! Unable to parse table from ${propName}: ${e.message}"
        println txt
        return "?"
    }
}