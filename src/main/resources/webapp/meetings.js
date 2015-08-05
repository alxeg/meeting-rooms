$(function() {
    var calOptions = {
        header : {
            left : '',
            center : '',
            right : ''
        },
        weekends : false,
        editable : false,
        defaultView : 'agendaDay',
        allDaySlot : false,
        lang : 'ru',
        minTime : '10:00',
        maxTime : '21:00',
        height : 750,
        displayEventEnd : true
    };
    
    function showLoading(elementId, show) {
        var loadingId = elementId+"_load";
        $("#"+loadingId).remove();
        if (show) {
            var parent = $("#"+elementId);
            var params = {
                    id: loadingId,
                    x: parent.offset().left+parent.width()/2 - 5,
                    y: parent.offset().top+parent.height()/3 - 5,
            }
            parent.append($($.templates("#tpl-loading").render(params)));
        } 
    }

    $.get('/api/rooms', function(data) {
        $.each(data, function(idx, room) {
            var calId = 'cal_' + room.id;
            var callLoadingError = function(id, status) {
                return function() {
                    showLoading(id, status);
                }
            };
            var opts = {
                room_data : {
                    id : calId,
                    title : room.name
                },

                eventSources : [ {
                    url : '/api/calendar/' + room.id,
                    type : 'GET',
                    error: function() {
                        showLoading(calId, true);
                    },
                    success: function() {
                        showLoading(calId, false);
                    }
                } ]
            };

            $.extend(opts, calOptions);

            $("#meetings_holder").append($($.templates("#tpl-room").render(opts.room_data)));
            $("#" + calId).fullCalendar(opts).everyTime("30s", function() {
                var cal = $(this);
                if (!cal.fullCalendar('getDate').stripTime().isSame(moment().stripTime())) {
                    cal.fullCalendar('today');
                } else {
                    cal.fullCalendar('refetchEvents');
                }
            });
        });
    });

});
