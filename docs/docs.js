$(document).ready(function(){
    $('.docs code').each(function(i, el){
       $(el).replaceWith("<pre class='brush: clojure'>" + $(el).html() + "</pre>");
    });
    SyntaxHighlighter.highlight();
});
