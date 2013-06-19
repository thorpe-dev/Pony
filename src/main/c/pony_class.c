#include <stdarg.h>
#include "pony_class.h"

extern unsigned int clazz_set_size;

unsigned long*
initialise_bit_set(unsigned int clazz_id)
{
    unsigned long* array = calloc(sizeof(unsigned long), clazz_set_size);

    array[clazz_id/64] = (1 << (clazz_id % 64));

    return array;
}

static_clazz*
initialise_static_class(unsigned int clazz_id, unsigned int method_count,
                        unsigned int * identifiers, pony_meth * methods)
{
  static_clazz* clazz = malloc(sizeof(static_clazz));
  clazz->clazz_set = initialise_bit_set(clazz_id);
  clazz->vtab = new_vtable(method_count, identifiers, methods);

  return clazz;
}

variable**
call_method(pony_clazz* this, unsigned int id, variable ** arguments)
{
  return lookup_meth(this->static_class_info->vtab, id)(this, arguments);
}

pony_meth
lookup_meth(vtable* v, unsigned int id)
{
  const vtable * cur = v;

  while (cur != NULL) {
    if (cur->identifier == id) {
      return cur->meth;
    }
    else {
      cur = cur->next;
    }
  }

  fprintf(stderr, "Could not find method with id %u\n", id);

  return NULL;
}

pony_clazz*
lookup_value(instance_variable* v, unsigned int id)
{
  instance_variable * cur = v;

  while (cur != NULL) {
    if (cur->identifier == id)
      return cur->value->clazz_value;
    else
      cur = cur->next;
  }

  fprintf(stderr, "Could not find method with id %u\n", id);

  return NULL;
}

// determines if one type is a subtype of the other.
bool
is_sub_type(unsigned long * current_class, unsigned long * other_class)
{

  for (unsigned int i = 0; i < clazz_set_size; ++i)
  {
    if ((current_class[i] && other_class[i]) != other_class[i])
      return false;
  }

  return true;
}

vtable*
new_vtable(unsigned int count, unsigned int * ids, pony_meth * methods)
{
  if (count == 0) {
    return NULL;
  }

  vtable * head = malloc(sizeof(vtable));
  vtable * cur = head;

  for (unsigned int i = 0; i < count; ++i)
  {
    cur->identifier = ids[i];
    cur->meth = methods[i];

    if (i == count - 1)
      cur->next = NULL;
    else {
      cur->next = malloc(sizeof(vtable));
      cur = cur->next;
    }
  }

  return head;
}

pony_clazz*
new_pony_clazz(static_clazz * id, unsigned long ** type_info)
{
  pony_clazz * new_clazz = malloc(sizeof(pony_clazz));
  new_clazz->static_class_info = id;
  new_clazz->type_info = type_info;

  return new_clazz;
}

void
create_instance_variables(pony_clazz * this, variable ** vars, unsigned int * ids, unsigned int count)
{
  if (count == 0) {
    this->variables = NULL;
    return;
  }

  instance_variable * head = malloc(sizeof(instance_variable));
  instance_variable * cur = head;

  for (unsigned int i = 0; i < count; ++i)
  {
    cur->identifier = ids[i];
    cur->value = vars[i];

    if (i == count - 1)
      cur->next = NULL;
    else {
      cur->next = malloc(sizeof(instance_variable));
      cur = cur->next;
    }
  }

  this->variables = head;
}

variable**
create_args(unsigned int count, ...)
{
  va_list ap;
  va_start(ap, count);


  variable** array = calloc(sizeof(variable*), count);
  for (unsigned int i = 0; i < count; ++i)
  {
    array[i] = va_arg(ap, variable*);
  }

  va_end(ap);

  return array;
}


// tagging main in so we can get a complete build
// when we append code to the base of this, we need to get rid of it.
int main(int argc, char const *argv[])
{
  initialise();
  return 0;
}