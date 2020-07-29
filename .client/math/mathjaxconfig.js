MathJax = {
    tex: {
      inlineMath: [['$', '$'], ['\\(', '\\)']]
    },
    startup: {
      pageReady() {
        const options = MathJax.startup.document.options;
        const BaseMathItem = options.MathItem;
        options.MathItem = class FixedMathItem extends BaseMathItem {
          assistiveMml(document) {
            if (this.display !== null) super.assistiveMml(document);
          }
        };
        return MathJax.startup.defaultPageReady();
      }
    }
  };