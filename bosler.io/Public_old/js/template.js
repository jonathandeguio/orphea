/* 
	Bosler by TEMPLATE STOCK
	templatestock.co @templatestock
	Released for free under the Creative Commons Attribution 3.0 license (templated.co/license)
*/


 $(window).scroll(function(){      
        /* -------------------
        Header Animation
        ---------------------*/
        if ($(this).scrollTop() > 50){  
            $('.before-color').addClass("after-color");
            document.getElementById("bosler-on-scroll").innerHTML = '<img class="bosler-icon-image" height="30" src="images/boslerlogo.svg" alt="Bosler" />';
            document.getElementById("language-on-scroll").innerHTML = '<img class="logo" src="images/translate.png">';
        }
        else{
            $('.before-color').removeClass("after-color");
            document.getElementById("bosler-on-scroll").innerHTML = '<img class="bosler-icon-image" height="30" src="images/boslerlogowhite.svg" alt="Bosler" />';
            document.getElementById("language-on-scroll").innerHTML = '<img class="logo" src="images/translatewhite.png">';

        }
    });

//full width revolution
var revapi;


jQuery(document).ready(function() {

    revapi = jQuery('.tp-banner').revolution(
            {
                delay: 6000,
                startwidth: 1170,
                startheight: 450,
                hideThumbs: 10,
                fullScreen: "on",
                forceFullWidth: "on",
                navigationStyle: "preview4"
            });

});	//ready

  $(window).load(function() {
    $('.testislider').flexslider({
        direction: "horizantol",
        animation: "slide",
        smoothHeight: true,
        controlNav: false
    });
  });

/**smooth scroll on anchor tag****/
$(function() {
    $('.scroll-to a[href*=#]:not([href=#])').click(function() {
        if (location.pathname.replace(/^\//, '') == this.pathname.replace(/^\//, '') && location.hostname == this.hostname) {
            var target = $(this.hash);
            target = target.length ? target : $('[name=' + this.hash.slice(1) + ']');
            if (target.length) {
                $('html,body').animate({
                    scrollTop: target.offset().top
                }, 1000);
                return false;
            }
        }
    });
});


/* -------------------
 Parallax Sections
 ---------------------*/
if (!Modernizr.touch) {
    $('.parallax-1').parallax("50%", 0.5);
    $('.parallax-2').parallax("50%", 0.5);
    $('.parallax-3').parallax("50%", 0.5);
}
/*----------------
 Auto Close Navbar
 -----------------*/
function close_toggle() {
    if ($(window).width() <= 992) {
        $('.navbar-collapse a').on('click', function() {
            $('.navbar-collapse').collapse('hide');
        });
    }
    else {
        $('.navbar .navbar-default a').off('click');
    }
}
close_toggle();
$(window).resize(close_toggle);
$(".navbar-collapse").css({maxHeight: $(window).height() - $(".navbar-header").height() + "px"});
$(function() {
    $('.navbar-toggle').bind('click', function(event) {
        var $anchor = $('.navbar-header');
        $('html, body').stop().animate({
            scrollTop: $($anchor).offset().top - 0
        }, 800, 'swing');
        event.preventDefault();
    });
});



/**prettyPhoto**/
$(window).load(function(){
 "use strict";
    $("a[data-gal^='prettyPhoto']").prettyPhoto();
  });


$(window).scroll(function() {

  var oTop = $('#counter').offset().top - window.innerHeight;
  if (a == 0 && $(window).scrollTop() > oTop) {
    $('.counter-value').each(function() {
      var $this = $(this),
        countTo = $this.attr('data-count');
      $({
        countNum: $this.text()
      }).animate({
          countNum: countTo
        },

        {

          duration: 2000,
          easing: 'swing',
          step: function() {
            $this.text(Math.floor(this.countNum));
          },
          complete: function() {
            $this.text(this.countNum);
            //alert('finished');
          }

        });
    });
    a = 500;
  }

});

// to show counting 
var a = 0;
$(window).scroll(function() {

    var oTop = $('#counter').offset().top - window.innerHeight;
    if (a == 0 && $(window).scrollTop() > oTop) {
      $('.counter-value').each(function() {
        var $this = $(this),
          countTo = $this.attr('data-count');
        $({
          countNum: $this.text()
        }).animate({
            countNum: countTo
          },
  
          {
  
            duration: 2000,
            easing: 'swing',
            step: function() {
              $this.text(Math.floor(this.countNum));
            },
            complete: function() {
              $this.text(this.countNum);
              //alert('finished');
            }
  
          });
      });
      a = 500;
    }
  
  });

// for dropdown menu
$("header .dropdown-menu").css('margin-top', 0);
        $(".dropdown")
            .mouseover(function() {
                $(this).addClass('show').attr('aria-expanded', "true");
                $(this).find('.dropdown-menu').addClass('show');
            })
            .mouseout(function() {
                $(this).removeClass('show').attr('aria-expanded', "false");
                $(this).find('.dropdown-menu').removeClass('show');
            });