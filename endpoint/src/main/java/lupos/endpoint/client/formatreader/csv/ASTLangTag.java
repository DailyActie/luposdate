/* Generated By:JJTree: Do not edit this line. ASTLangTag.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package lupos.endpoint.client.formatreader.csv;

public
class ASTLangTag extends SimpleNode {
	
	private String langTag;

	public ASTLangTag(int id) {
		super(id);
	}

	public ASTLangTag(CSVParser p, int id) {
		super(p, id);
	}

	public String getLangTag() {
		return langTag;
	}

	public void setLangTag(String langTag) {
		this.langTag = langTag;
	}

	@Override
	public String toString() {
		return super.toString()+" "+langTag;
	}
}
/* JavaCC - OriginalChecksum=c0fe129f7bf371057f1fe326034ca531 (do not edit this line) */
