package net.sourceforge.htmlunit.htmlparser;

public enum HTMLElement {
    HTML,
    HEAD(HTML),
    BODY(HTML, new HTMLElement[] {HEAD}),
    H1(BODY),
    P(BODY);

    static {
        P.closes_ = new HTMLElement[] {P};
    }

    //private enum FLAG {INLINE, BLOCK, EMPTY, CONTAINER, SPECIAL};
    
    private HTMLElement parent_;
    private HTMLElement[] closes_;

    HTMLElement() {
        this(null);
    }

    HTMLElement(HTMLElement parent) {
        this(parent, TagBalancer.EMPTY_ELEMENTS);
    }

    HTMLElement(HTMLElement parent, HTMLElement[] closes) {
        parent_ = parent;
        closes_ = closes;
    }

    public HTMLElement getParent() {
        return parent_;
    }

    public HTMLElement[] getCloses() {
        return closes_;
    }
}
