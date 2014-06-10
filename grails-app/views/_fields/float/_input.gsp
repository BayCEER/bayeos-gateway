<!--  Overwrite default rendering as number without step="any" -->

<g:if test="${required}">
   <input id="${property}" class="form-control" type="text" required="" value="${value}" name="${property}">
</g:if>
<g:else>
   <input id="${property}" class="form-control" type="text" value="${value}" name="${property}">
</g:else>

